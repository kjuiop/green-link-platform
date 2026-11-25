package com.greenlink.shop.adapter.out.identity;

import com.greenlink.shopcore.application.auth.dto.AccountProfile;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OnurimIdentityVerifierTest {

    private static final String BASE = "https://onurim.test";
    private static final String ACCOUNT_JSON =
        "{\"adminId\":5,\"loginId\":\"shop01\",\"name\":\"홍길동\",\"companyId\":7,\"companyName\":\"테스트업체\","
        + "\"companyOnurimEnabled\":true,\"companySiteEnabled\":false,\"adminOnurimEnabled\":true,\"superAdmin\":false}";

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
}
