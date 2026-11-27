package com.greenlink.shop.adapter.out.identity;

import com.greenlink.shopcore.application.auth.dto.AccountProfile;
import com.greenlink.shopcore.application.auth.dto.ManagedAccountCommand;
import com.greenlink.shopcore.application.auth.dto.RegisteredAccount;
import com.greenlink.shopcore.application.auth.dto.SignupCommand;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationStatus;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationView;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplyCommand;
import com.greenlink.shopcore.application.onurim.dto.OnurimCompanyType;
import com.greenlink.shopcore.application.onurim.dto.OnurimDocument;
import com.greenlink.shopcore.application.onurim.dto.OnurimDocumentRequirement;
import com.greenlink.shopcore.application.onurim.dto.OnurimRegion;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OnurimIdentityVerifierTest {

    private static final String BASE = "https://onurim.test";
    private static final String ACCOUNT_JSON =
        "{\"adminId\":5,\"loginId\":\"shop01\",\"name\":\"홍길동\",\"companyId\":7,\"companyName\":\"테스트업체\","
        + "\"companyOnurimEnabled\":true,\"companySiteEnabled\":false,\"adminOnurimEnabled\":true,\"role\":\"PARTNER_ADMIN\"}";

    private MockRestServiceServer server;
    private OnurimIdentityVerifier verifier;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        verifier = new OnurimIdentityVerifier(builder.build(), "s3cret");
    }

    @Test
    void 검증_성공시_계정정보를_매핑하고_시크릿_헤더를_보낸다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/verify"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andRespond(withSuccess(ACCOUNT_JSON, MediaType.APPLICATION_JSON));

        VerifiedIdentity id = verifier.verify("shop01", "pw");

        assertThat(id.adminId()).isEqualTo(5L);
        assertThat(id.loginId()).isEqualTo("shop01");
        assertThat(id.companyId()).isEqualTo(7L);
        assertThat(id.companyName()).isEqualTo("테스트업체");
        assertThat(id.companyOnurimEnabled()).isTrue();
        assertThat(id.companySiteEnabled()).isFalse();
        assertThat(id.adminOnurimEnabled()).isTrue();
        assertThat(id.superAdmin()).isFalse();
        server.verify();
    }

    @Test
    void 슈퍼관리자_역할은_superAdmin으로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/verify"))
            .andRespond(withSuccess(ACCOUNT_JSON.replace("PARTNER_ADMIN", "SUPER_ADMIN"), MediaType.APPLICATION_JSON));

        assertThat(verifier.verify("shop01", "pw").superAdmin()).isTrue();
        server.verify();
    }

    @Test
    void 알_수_없는_필드가_있어도_파싱된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/accounts/5"))
            .andRespond(withSuccess(ACCOUNT_JSON.replace("}", ",\"futureField\":1}"), MediaType.APPLICATION_JSON));

        assertThat(verifier.getAccount(5L).adminId()).isEqualTo(5L);
    }

    @Test
    void 배치_조회는_ID_목록을_보내고_프로필로_매핑한다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/accounts/batch"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andExpect(content().json("{\"adminIds\":[5,6]}"))
            .andRespond(withSuccess(
                "[{\"adminId\":5,\"loginId\":\"shop01\",\"name\":\"홍길동\",\"companyId\":7,\"companyName\":\"테스트업체\"},"
                + "{\"adminId\":6,\"loginId\":\"shop02\",\"name\":\"김철수\",\"companyId\":null,\"companyName\":null}]",
                MediaType.APPLICATION_JSON));

        List<AccountProfile> result = verifier.getAccounts(List.of(5L, 6L));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("홍길동");
        assertThat(result.get(0).companyName()).isEqualTo("테스트업체");
        assertThat(result.get(1).companyName()).isNull();
        server.verify();
    }

    @Test
    void 배치_조회는_100건_단위로_나눠_호출한다() {
        List<Long> ids = java.util.stream.LongStream.rangeClosed(1, 101).boxed().toList();
        server.expect(requestTo(BASE + "/internal/shop-auth/accounts/batch"))
            .andExpect(jsonPath("$.adminIds.length()").value(100))
            .andRespond(withSuccess("[{\"adminId\":1,\"loginId\":\"a\",\"name\":\"A\"}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/internal/shop-auth/accounts/batch"))
            .andExpect(jsonPath("$.adminIds.length()").value(1))
            .andRespond(withSuccess("[{\"adminId\":101,\"loginId\":\"b\",\"name\":\"B\"}]", MediaType.APPLICATION_JSON));

        List<AccountProfile> result = verifier.getAccounts(ids);

        assertThat(result).extracting(AccountProfile::adminId).containsExactly(1L, 101L);
        server.verify();
    }

    @Test
    void 배치_조회는_빈_목록이면_호출하지_않는다() {
        assertThat(verifier.getAccounts(List.of())).isEmpty();
        server.verify();
    }

    @Test
    void 배치_조회_장애는_업스트림_장애로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/accounts/batch"))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> verifier.getAccounts(List.of(5L)))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));
    }

    @Test
    void 계정_재확인_성공() {
        server.expect(requestTo(BASE + "/internal/shop-auth/accounts/5"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andRespond(withSuccess(ACCOUNT_JSON, MediaType.APPLICATION_JSON));

        assertThat(verifier.getAccount(5L).adminId()).isEqualTo(5L);
    }

    @Test
    void 인증_실패_401은_자격증명_오류로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/verify"))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_005\",\"message\":\"x\"}"));

        assertThatThrownBy(() -> verifier.verify("shop01", "bad"))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_INVALID_CREDENTIALS));
    }

    @Test
    void 비활성_승인대기_403은_계정비활성으로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/accounts/5"))
            .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_006\",\"message\":\"x\"}"));
        server.expect(requestTo(BASE + "/internal/shop-auth/accounts/6"))
            .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_007\",\"message\":\"x\"}"));

        for (long id : new long[]{5L, 6L}) {
            assertThatThrownBy(() -> verifier.getAccount(id))
                .isInstanceOfSatisfying(ShopBusinessException.class,
                    e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_ACCOUNT_INACTIVE));
        }
    }

    @Test
    void 시크릿_불일치_403은_업스트림_장애로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/verify"))
            .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_012\",\"message\":\"x\"}"));

        assertThatThrownBy(() -> verifier.verify("shop01", "pw"))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));
    }

    @Test
    void 서버_오류_5xx는_업스트림_장애로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/verify"))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> verifier.verify("shop01", "pw"))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));
    }

    @Test
    void 연결_실패_타임아웃은_업스트림_장애로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/verify"))
            .andRespond(withException(new IOException("timeout")));

        assertThatThrownBy(() -> verifier.verify("shop01", "pw"))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));
    }

    @Test
    void 시크릿이_비어있으면_호출없이_업스트림_장애로_처리한다() {
        OnurimIdentityVerifier noSecret = new OnurimIdentityVerifier(RestClient.builder().baseUrl(BASE).build(), " ");

        assertThatThrownBy(() -> noSecret.verify("shop01", "pw"))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));
    }

    private static final SignupCommand SIGNUP = new SignupCommand("shop01", "Passw0rd!", "홍길동", "010-1234-5678", null,
        "123-45-67890", "꽃집", "김대표", null);

    @Test
    void 가입_성공시_계정과_대상회사_창립자_여부를_매핑한다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/signup"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andExpect(jsonPath("$.loginId").value("shop01"))
            .andExpect(jsonPath("$.businessNo").value("123-45-67890"))
            .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                .body("{\"account\":" + ACCOUNT_JSON + ",\"targetCompanyId\":7,\"founder\":true}"));

        RegisteredAccount result = verifier.register(SIGNUP);

        assertThat(result.identity().adminId()).isEqualTo(5L);
        assertThat(result.targetCompanyId()).isEqualTo(7L);
        assertThat(result.founder()).isTrue();
        server.verify();
    }

    @Test
    void 가입_409는_코드별로_중복_오류로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/signup"))
            .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_015\",\"message\":\"x\"}"));
        server.expect(requestTo(BASE + "/internal/shop-auth/signup"))
            .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_016\",\"message\":\"x\"}"));

        assertThatThrownBy(() -> verifier.register(SIGNUP)).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.SIGNUP_LOGIN_ID_DUPLICATED));
        assertThatThrownBy(() -> verifier.register(SIGNUP)).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.SIGNUP_BUSINESS_NO_REGISTERED));
    }

    @Test
    void 가입_400은_입력_오류로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/signup"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_018\",\"message\":\"x\"}"));

        assertThatThrownBy(() -> verifier.register(SIGNUP)).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.SIGNUP_INVALID_INPUT));
    }

    @Test
    void 가입_서버오류와_시크릿_불일치는_업스트림_장애로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/signup"))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        server.expect(requestTo(BASE + "/internal/shop-auth/signup"))
            .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_012\",\"message\":\"x\"}"));

        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> verifier.register(SIGNUP)).isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));
        }
    }

    @Test
    void 사업자번호_존재_조회는_exists만_읽는다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/business-numbers/exists"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andExpect(content().json("{\"businessNo\":\"123-45-67890\"}"))
            .andRespond(withSuccess("{\"exists\":true}", MediaType.APPLICATION_JSON));

        assertThat(verifier.businessNoExists("123-45-67890")).isTrue();
        server.verify();
    }

    @Test
    void 기존_엔드포인트의_400은_가입_오류가_아닌_업스트림_장애로_남는다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/verify"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> verifier.verify("shop01", "pw")).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));
    }

    @Test
    void 접근_부여는_어드민과_회사_ID를_보내고_시크릿_헤더를_붙인다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/access-grants"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andExpect(jsonPath("$.adminId").value(5))
            .andExpect(jsonPath("$.companyId").value(7))
            .andRespond(withSuccess(ACCOUNT_JSON, MediaType.APPLICATION_JSON));

        verifier.grant(5L, 7L);

        server.verify();
    }

    @Test
    void 접근_부여_회사_불일치_409는_ACCESS_COMPANY_MISMATCH로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/access-grants"))
            .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_020\"}"));

        assertThatThrownBy(() -> verifier.grant(5L, 7L))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.ACCESS_COMPANY_MISMATCH));
    }

    @Test
    void 접근_부여_계정_또는_회사_없음_404는_ADMIN_REQUEST_NOT_FOUND로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/access-grants"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_022\"}"));

        assertThatThrownBy(() -> verifier.grant(5L, 7L))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.ADMIN_REQUEST_NOT_FOUND));
    }

    @Test
    void 접근_부여_그_외_오류는_업스트림_장애로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/access-grants"))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> verifier.grant(5L, 7L))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));
    }

    @Test
    void 회사명_배치_조회는_ID_목록을_보내고_맵으로_매핑한다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/companies/batch"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andExpect(jsonPath("$.companyIds[0]").value(7))
            .andRespond(withSuccess("[{\"companyId\":7,\"companyName\":\"꽃집\"}]", MediaType.APPLICATION_JSON));

        assertThat(verifier.getCompanyNames(java.util.List.of(7L))).containsEntry(7L, "꽃집");
        server.verify();
    }

    @Test
    void 회사명_조회_대상이_비어_있으면_호출하지_않는다() {
        assertThat(verifier.getCompanyNames(java.util.List.of())).isEmpty();
        server.verify();
    }

    @Test
    void 비밀번호_미설정_403은_AUTH_PASSWORD_NOT_SET으로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/verify"))
            .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_023\",\"message\":\"x\"}"));

        assertThatThrownBy(() -> verifier.verify("brand01", "pw")).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_PASSWORD_NOT_SET));
    }

    @Test
    void 관리_계정_생성은_명령을_보내고_계정을_매핑한다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/managed-accounts"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andExpect(jsonPath("$.loginId").value("brand01"))
            .andExpect(jsonPath("$.phone").value("010-1234-5678"))
            .andExpect(jsonPath("$.companyId").value(7))
            .andRespond(withSuccess(ACCOUNT_JSON, MediaType.APPLICATION_JSON));

        VerifiedIdentity id = verifier.createManagedAccount(
            new ManagedAccountCommand("brand01", "홍길동", "010-1234-5678", null, 7L));

        assertThat(id.adminId()).isEqualTo(5L);
        server.verify();
    }

    @Test
    void 관리_계정_생성_아이디_중복_409는_SIGNUP_LOGIN_ID_DUPLICATED로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/managed-accounts"))
            .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_015\",\"message\":\"x\"}"));

        assertThatThrownBy(() -> verifier.createManagedAccount(
            new ManagedAccountCommand("brand01", "홍길동", "010-1234-5678", null, 7L)))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.SIGNUP_LOGIN_ID_DUPLICATED));
    }

    @Test
    void 비밀번호_설정은_값을_보내고_본문_없는_응답을_받는다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/password-setup"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andExpect(jsonPath("$.loginId").value("brand01"))
            .andExpect(jsonPath("$.phone").value("010-1234-5678"))
            .andExpect(jsonPath("$.newPassword").value("Passw0rd!"))
            .andRespond(withStatus(HttpStatus.NO_CONTENT));

        verifier.completePasswordSetup("brand01", "010-1234-5678", "Passw0rd!");

        server.verify();
    }

    @Test
    void 비밀번호_설정_400은_단일_실패로_매핑되고_서버오류는_장애로_남는다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/password-setup"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_024\",\"message\":\"x\"}"));
        server.expect(requestTo(BASE + "/internal/shop-auth/password-setup"))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> verifier.completePasswordSetup("brand01", "010", "Passw0rd!"))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_PASSWORD_SETUP_FAILED));
        assertThatThrownBy(() -> verifier.completePasswordSetup("brand01", "010", "Passw0rd!"))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));
    }

    @Test
    void 재발급은_어드민_ID로_호출하고_이미_설정된_409는_NOT_PENDING으로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/accounts/5/password-setup-reissue"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andRespond(withStatus(HttpStatus.NO_CONTENT));
        server.expect(requestTo(BASE + "/internal/shop-auth/accounts/5/password-setup-reissue"))
            .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_025\",\"message\":\"x\"}"));

        verifier.reissuePasswordSetup(5L);
        assertThatThrownBy(() -> verifier.reissuePasswordSetup(5L)).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_PASSWORD_SETUP_NOT_PENDING));
    }

    private static final String APPLICATION_JSON =
        "{\"id\":3,\"companyId\":7,\"status\":\"REJECTED\",\"desiredType\":\"BOTH\",\"approvedType\":null,"
        + "\"rejectReason\":\"서류 미비\",\"processedAt\":\"2026-11-26T10:00:00\",\"createdAt\":\"2026-11-25T09:30:00\","
        + "\"faxNumber\":\"021112222\",\"futureField\":1}";

    private static OnurimApplyCommand applyCommand() {
        return new OnurimApplyCommand(OnurimCompanyType.BOTH, "02-111-2222", "서울시 강남구", "101호", "a@b.com", "메모",
            "021112222", "국민", "123-456", "홍길동", List.of(new OnurimRegion("서울", "강남구")), null, null);
    }

    private static OnurimApplyCommand applyCommandWithDocs() {
        OnurimApplyCommand c = applyCommand();
        return new OnurimApplyCommand(c.desiredType(), c.companyPhone(), c.address(), c.detailAddress(),
            c.taxInvoiceEmail(), c.memo(), c.faxNumber(), c.bankName(), c.accountNumber(), c.accountHolder(),
            c.regions(), new OnurimDocument("biz.png", "image/png", new byte[]{1, 2, 3}),
            new OnurimDocument("bank.pdf", "application/pdf", new byte[]{4, 5}));
    }

    @Test
    void 신청은_관리자_회사_ID와_본문을_보내고_응답을_매핑한다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
            .andExpect(content().string(containsString("name=\"data\"")))
            .andExpect(content().string(containsString("\"adminId\":5")))
            .andExpect(content().string(containsString("\"companyId\":7")))
            .andExpect(content().string(containsString("\"desiredType\":\"BOTH\"")))
            .andExpect(content().string(containsString("\"companyPhone\":\"02-111-2222\"")))
            .andExpect(content().string(containsString("\"sido\":\"서울\"")))
            .andExpect(content().string(not(containsString("businessDoc"))))
            .andExpect(content().string(not(containsString("bankDoc"))))
            .andRespond(withSuccess(APPLICATION_JSON, MediaType.APPLICATION_JSON));

        OnurimApplicationView view = verifier.apply(5L, 7L, applyCommand());

        assertThat(view.id()).isEqualTo(3L);
        assertThat(view.status()).isEqualTo(OnurimApplicationStatus.REJECTED);
        assertThat(view.desiredType()).isEqualTo(OnurimCompanyType.BOTH);
        assertThat(view.rejectReason()).isEqualTo("서류 미비");
        assertThat(view.processedAt()).isEqualTo(java.time.LocalDateTime.of(2026, 11, 26, 10, 0));
        server.verify();
    }

    @Test
    void 서류가_있으면_파일_파트로_함께_보낸다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
            .andExpect(content().string(containsString("name=\"businessDoc\"; filename=\"biz.png\"")))
            .andExpect(content().string(containsString("Content-Type: image/png")))
            .andExpect(content().string(containsString("name=\"bankDoc\"; filename=\"bank.pdf\"")))
            .andExpect(content().string(containsString("Content-Type: application/pdf")))
            .andRespond(withSuccess(APPLICATION_JSON, MediaType.APPLICATION_JSON));

        assertThat(verifier.apply(5L, 7L, applyCommandWithDocs()).id()).isEqualTo(3L);
        server.verify();
    }

    @Test
    void 서류_오류_응답은_전용_코드로_매핑된다() {
        String[][] cases = {
            {"400", "ONURIM_APP_009"}, {"400", "ONURIM_APP_010"}, {"400", "VENDOR_006"}, {"400", "VENDOR_007"},
            {"413", "-"}, {"500", "PHOTO_002"}};
        for (String[] c : cases) {
            server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications"))
                .andRespond(withStatus(HttpStatus.valueOf(Integer.parseInt(c[0]))).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"code\":\"" + c[1] + "\",\"message\":\"x\"}"));
        }

        assertApplyError(ShopErrorCode.ONURIM_DOCUMENT_REQUIRED);
        assertApplyError(ShopErrorCode.ONURIM_DOCUMENT_NOT_ALLOWED);
        assertApplyError(ShopErrorCode.ONURIM_DOCUMENT_INVALID);
        assertApplyError(ShopErrorCode.ONURIM_DOCUMENT_INVALID);
        assertApplyError(ShopErrorCode.ONURIM_DOCUMENT_INVALID);
        assertApplyError(ShopErrorCode.ONURIM_DOCUMENT_UPLOAD_FAILED);
    }

    @Test
    void 서류_필요_여부_조회는_관리자_회사_ID로_호출한다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications/documents?adminId=5&companyId=7"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andRespond(withSuccess("{\"needsBusinessReg\":true,\"needsBankBook\":false}", MediaType.APPLICATION_JSON));

        OnurimDocumentRequirement result = verifier.getDocumentRequirement(5L, 7L);

        assertThat(result.needsBusinessReg()).isTrue();
        assertThat(result.needsBankBook()).isFalse();
        server.verify();
    }

    @Test
    void 서류_필요_여부_조회_오류는_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications/documents?adminId=5&companyId=7"))
            .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"ONURIM_APP_008\",\"message\":\"x\"}"));

        assertThatThrownBy(() -> verifier.getDocumentRequirement(5L, 7L))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN));
    }

    @Test
    void 신청_오류_응답은_전용_코드로_매핑된다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications"))
            .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"ONURIM_APP_002\",\"message\":\"x\"}"));
        server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications"))
            .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"ONURIM_APP_003\",\"message\":\"x\"}"));
        server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"ONURIM_APP_005\",\"message\":\"x\"}"));
        server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications"))
            .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"ONURIM_APP_008\",\"message\":\"x\"}"));
        server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"AUTH_022\",\"message\":\"x\"}"));
        server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications"))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertApplyError(ShopErrorCode.ONURIM_ALREADY_ENABLED);
        assertApplyError(ShopErrorCode.ONURIM_APPLICATION_ALREADY_PENDING);
        assertApplyError(ShopErrorCode.ONURIM_APPLICATION_INVALID);
        assertApplyError(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        assertApplyError(ShopErrorCode.ADMIN_REQUEST_NOT_FOUND);
        assertApplyError(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
    }

    private void assertApplyError(ShopErrorCode expected) {
        assertThatThrownBy(() -> verifier.apply(5L, 7L, applyCommand()))
            .isInstanceOfSatisfying(ShopBusinessException.class, e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 최신_신청_조회는_회사_ID로_호출하고_알_수_없는_필드를_무시한다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications/latest?companyId=7"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("X-Internal-Secret", "s3cret"))
            .andRespond(withSuccess(APPLICATION_JSON, MediaType.APPLICATION_JSON));

        assertThat(verifier.findLatest(7L)).get()
            .extracting(OnurimApplicationView::id).isEqualTo(3L);
    }

    @Test
    void 신청_이력이_없으면_204를_빈_값으로_돌려준다() {
        server.expect(requestTo(BASE + "/internal/shop-auth/onurim-applications/latest?companyId=7"))
            .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertThat(verifier.findLatest(7L)).isEmpty();
    }
}
