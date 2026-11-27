package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.global.exception.GlobalExceptionHandler;
import com.greenlink.core.application.auth.dto.AccountSummary;
import com.greenlink.core.application.auth.dto.CompanySummary;
import com.greenlink.core.application.auth.dto.ShopSignupCommand;
import com.greenlink.core.application.auth.dto.ShopSignupResult;
import com.greenlink.core.application.auth.dto.VerifiedAccount;
import com.greenlink.core.application.auth.usecase.ShopAccessUseCase;
import com.greenlink.core.application.auth.usecase.ShopManagedAccountUseCase;
import com.greenlink.core.application.auth.usecase.ShopSignupUseCase;
import com.greenlink.core.application.auth.usecase.VerifyAdminAccountUseCase;
import com.greenlink.core.application.onurimapplication.dto.OnurimApplicationResult;
import com.greenlink.core.application.onurimapplication.dto.OnurimDocumentRequirement;
import com.greenlink.core.application.onurimapplication.usecase.ApplyOnurimUseCase;
import com.greenlink.core.application.onurimapplication.usecase.ApplyOnurimWithDocumentsUseCase;
import com.greenlink.core.application.vendor.dto.SignupDocument;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationStatus;
import com.greenlink.core.global.exception.onurimapplication.OnurimApplicationErrorCode;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InternalShopAuthControllerTest {

    private static final String BODY = "{\"loginId\":\"admin01\",\"password\":\"rawPw\"}";
    private static final VerifiedAccount ACCOUNT = new VerifiedAccount(1L, "admin01", "관리자", 7L, "테스트업체", true, false, true, false);

    private static final String SIGNUP_BODY = "{\"loginId\":\"shop01\",\"password\":\"Passw0rd!\",\"name\":\"홍길동\","
            + "\"phone\":\"010-1234-5678\",\"email\":\"a@test.com\",\"businessNo\":\"123-45-67890\","
            + "\"companyName\":\"꽃집\",\"ceoName\":\"김대표\"}";

    private final VerifyAdminAccountUseCase useCase = mock(VerifyAdminAccountUseCase.class);
    private final ShopSignupUseCase signupUseCase = mock(ShopSignupUseCase.class);
    private final ShopAccessUseCase accessUseCase = mock(ShopAccessUseCase.class);
    private final ShopManagedAccountUseCase managedUseCase = mock(ShopManagedAccountUseCase.class);
    private final ApplyOnurimUseCase onurimUseCase = mock(ApplyOnurimUseCase.class);
    private final ApplyOnurimWithDocumentsUseCase onurimWithDocsUseCase = mock(ApplyOnurimWithDocumentsUseCase.class);

    private MockMvc mvc(String secret) {
        return MockMvcBuilders.standaloneSetup(new InternalShopAuthController(useCase, signupUseCase, accessUseCase, managedUseCase, onurimUseCase, onurimWithDocsUseCase, secret))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 시크릿_설정이_비어있으면_헤더가_있어도_403() throws Exception {
        mvc("").perform(post("/internal/shop-auth/verify")
                        .header("X-Internal-Secret", "anything")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(useCase);
    }

    @Test
    void 헤더가_없으면_403() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/verify")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(useCase);
    }

    @Test
    void 시크릿이_틀리면_403() throws Exception {
        mvc("s3cret").perform(get("/internal/shop-auth/accounts/1")
                        .header("X-Internal-Secret", "wrong"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(useCase);
    }

    @Test
    void verify_정상_시크릿이면_계정_정보를_반환() throws Exception {
        when(useCase.verify("admin01", "rawPw")).thenReturn(ACCOUNT);

        mvc("s3cret").perform(post("/internal/shop-auth/verify")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminId").value(1))
                .andExpect(jsonPath("$.loginId").value("admin01"))
                .andExpect(jsonPath("$.name").value("관리자"))
                .andExpect(jsonPath("$.companyId").value(7))
                .andExpect(jsonPath("$.companyName").value("테스트업체"))
                .andExpect(jsonPath("$.companyOnurimEnabled").value(true))
                .andExpect(jsonPath("$.companySiteEnabled").value(false))
                .andExpect(jsonPath("$.adminOnurimEnabled").value(true))
                .andExpect(jsonPath("$.superAdmin").value(false));
    }

    @Test
    void verify_자격_증명_오류는_401로_매핑() throws Exception {
        when(useCase.verify(anyString(), anyString()))
                .thenThrow(new BusinessException(AuthErrorCode.INVALID_CREDENTIALS));

        mvc("s3cret").perform(post("/internal/shop-auth/verify")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_005"));
    }

    @Test
    void accounts_정상_시크릿이면_계정_정보를_반환() throws Exception {
        when(useCase.getActiveAccount(1L)).thenReturn(ACCOUNT);

        mvc("s3cret").perform(get("/internal/shop-auth/accounts/1")
                        .header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminId").value(1));
    }

    @Test
    void batch_시크릿이_틀리면_403() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/accounts/batch")
                        .header("X-Internal-Secret", "wrong")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"adminIds\":[1]}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(useCase);
    }

    @Test
    void batch_정상_시크릿이면_요약_목록을_반환() throws Exception {
        when(useCase.getAccountSummaries(List.of(1L, 2L))).thenReturn(List.of(
                new AccountSummary(1L, "admin01", "관리자", 7L, "테스트업체", false),
                new AccountSummary(2L, "admin02", "직원", null, null, false)));

        mvc("s3cret").perform(post("/internal/shop-auth/accounts/batch")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"adminIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].adminId").value(1))
                .andExpect(jsonPath("$[0].loginId").value("admin01"))
                .andExpect(jsonPath("$[0].name").value("관리자"))
                .andExpect(jsonPath("$[0].companyId").value(7))
                .andExpect(jsonPath("$[0].companyName").value("테스트업체"))
                .andExpect(jsonPath("$[1].companyId").isEmpty());
    }

    @Test
    void batch_adminIds가_없으면_400() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/accounts/batch")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(useCase);
    }

    @Test
    void batch_100건을_초과하면_400() throws Exception {
        String ids = IntStream.rangeClosed(1, 101).mapToObj(String::valueOf).collect(Collectors.joining(","));

        mvc("s3cret").perform(post("/internal/shop-auth/accounts/batch")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"adminIds\":[" + ids + "]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(useCase);
    }

    @Test
    void accounts_정지_계정은_403으로_매핑() throws Exception {
        when(useCase.getActiveAccount(anyLong()))
                .thenThrow(new BusinessException(AuthErrorCode.ACCOUNT_INACTIVE));

        mvc("s3cret").perform(get("/internal/shop-auth/accounts/1")
                        .header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_006"));
    }

    @Test
    void signup_시크릿이_틀리면_403() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/signup")
                        .header("X-Internal-Secret", "wrong")
                        .contentType(MediaType.APPLICATION_JSON).content(SIGNUP_BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(signupUseCase);
    }

    @Test
    void signup_정상이면_결과를_반환하고_커맨드로_변환한다() throws Exception {
        when(signupUseCase.signup(any(ShopSignupCommand.class)))
                .thenReturn(new ShopSignupResult(new VerifiedAccount(10L, "shop01", "홍길동", 20L, "꽃집", false, true, true, false), 20L, true));

        mvc("s3cret").perform(post("/internal/shop-auth/signup")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content(SIGNUP_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.founder").value(true))
                .andExpect(jsonPath("$.targetCompanyId").value(20))
                .andExpect(jsonPath("$.account.adminId").value(10))
                .andExpect(jsonPath("$.account.companyOnurimEnabled").value(false));
    }

    @Test
    void signup_입력_검증에_실패하면_400() throws Exception {
        String bad = SIGNUP_BODY.replace("Passw0rd!", "short");

        mvc("s3cret").perform(post("/internal/shop-auth/signup")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content(bad))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(signupUseCase);
    }

    @Test
    void signup_이미_등록된_사업자번호는_409로_매핑() throws Exception {
        when(signupUseCase.signup(any(ShopSignupCommand.class)))
                .thenThrow(new BusinessException(AuthErrorCode.SHOP_BUSINESS_NO_REGISTERED));

        mvc("s3cret").perform(post("/internal/shop-auth/signup")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content(SIGNUP_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH_016"));
    }

    @Test
    void 사업자번호_존재_조회_시크릿이_틀리면_403() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/business-numbers/exists")
                        .header("X-Internal-Secret", "wrong")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"businessNo\":\"123-45-67890\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(signupUseCase);
    }

    @Test
    void 사업자번호_존재_조회는_존재_여부만_반환한다() throws Exception {
        when(signupUseCase.existsBusinessNo("123-45-67890")).thenReturn(true);

        mvc("s3cret").perform(post("/internal/shop-auth/business-numbers/exists")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"businessNo\":\"123-45-67890\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void 접근_부여_시크릿이_틀리면_403() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/access-grants")
                        .header("X-Internal-Secret", "wrong")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"adminId\":1,\"companyId\":7}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(accessUseCase);
    }

    @Test
    void 접근_부여_정상이면_계정을_반환한다() throws Exception {
        when(accessUseCase.grant(1L, 7L)).thenReturn(ACCOUNT);

        mvc("s3cret").perform(post("/internal/shop-auth/access-grants")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"adminId\":1,\"companyId\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminId").value(1))
                .andExpect(jsonPath("$.companyId").value(7));
    }

    @Test
    void 접근_부여_필수값이_없으면_400() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/access-grants")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"adminId\":1}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(accessUseCase);
    }

    @Test
    void 접근_부여_다른_회사_소속이면_409로_매핑() throws Exception {
        when(accessUseCase.grant(1L, 7L)).thenThrow(new BusinessException(AuthErrorCode.SHOP_ACCESS_COMPANY_MISMATCH));

        mvc("s3cret").perform(post("/internal/shop-auth/access-grants")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"adminId\":1,\"companyId\":7}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH_020"));
    }

    @Test
    void 접근_부여_없는_회사는_404로_매핑() throws Exception {
        when(accessUseCase.grant(1L, 7L)).thenThrow(new BusinessException(AuthErrorCode.SHOP_ACCESS_COMPANY_NOT_FOUND));

        mvc("s3cret").perform(post("/internal/shop-auth/access-grants")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"adminId\":1,\"companyId\":7}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AUTH_021"));
    }

    @Test
    void 회사_일괄_조회_시크릿이_틀리면_403() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/companies/batch")
                        .header("X-Internal-Secret", "wrong")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"companyIds\":[7]}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(accessUseCase);
    }

    @Test
    void 회사_일괄_조회는_회사명을_반환한다() throws Exception {
        when(accessUseCase.getCompanySummaries(List.of(7L))).thenReturn(List.of(new CompanySummary(7L, "꽃집")));

        mvc("s3cret").perform(post("/internal/shop-auth/companies/batch")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"companyIds\":[7]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyId").value(7))
                .andExpect(jsonPath("$[0].companyName").value("꽃집"));
    }

    @Test
    void 회사_일괄_조회는_100건을_넘으면_400() throws Exception {
        String ids = IntStream.rangeClosed(1, 101).mapToObj(String::valueOf).collect(Collectors.joining(","));

        mvc("s3cret").perform(post("/internal/shop-auth/companies/batch")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"companyIds\":[" + ids + "]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(accessUseCase);
    }

    private static final String MANAGED_BODY = "{\"loginId\":\"brand01\",\"name\":\"홍길동\","
            + "\"phone\":\"010-1234-5678\",\"companyId\":7}";
    private static final String SETUP_BODY = "{\"loginId\":\"brand01\",\"phone\":\"010-1234-5678\",\"newPassword\":\"Passw0rd!\"}";

    @Test
    void managed_accounts_시크릿이_틀리면_403() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/managed-accounts")
                        .header("X-Internal-Secret", "wrong")
                        .contentType(MediaType.APPLICATION_JSON).content(MANAGED_BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(managedUseCase);
    }

    @Test
    void managed_accounts_휴대폰이_없으면_400() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/managed-accounts")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"brand01\",\"name\":\"홍길동\",\"companyId\":7}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(managedUseCase);
    }

    @Test
    void managed_accounts_정상이면_계정을_반환() throws Exception {
        when(managedUseCase.create(any())).thenReturn(
                new VerifiedAccount(10L, "brand01", "홍길동", 7L, "파트너사", false, true, false, false));

        mvc("s3cret").perform(post("/internal/shop-auth/managed-accounts")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content(MANAGED_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminId").value(10))
                .andExpect(jsonPath("$.adminOnurimEnabled").value(false));
    }

    @Test
    void password_setup_시크릿이_틀리면_403() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/password-setup")
                        .header("X-Internal-Secret", "wrong")
                        .contentType(MediaType.APPLICATION_JSON).content(SETUP_BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(managedUseCase);
    }

    @Test
    void password_setup_정상이면_204() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/password-setup")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content(SETUP_BODY))
                .andExpect(status().isNoContent());
        verify(managedUseCase).completePasswordSetup("brand01", "010-1234-5678", "Passw0rd!");
    }

    @Test
    void password_setup_약한_비밀번호는_400() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/password-setup")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"brand01\",\"phone\":\"010-1234-5678\",\"newPassword\":\"12345678\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(managedUseCase);
    }

    @Test
    void password_setup_실패는_단일_에러코드로_매핑() throws Exception {
        org.mockito.Mockito.doThrow(new BusinessException(AuthErrorCode.PASSWORD_SETUP_FAILED))
                .when(managedUseCase).completePasswordSetup(anyString(), anyString(), anyString());

        mvc("s3cret").perform(post("/internal/shop-auth/password-setup")
                        .header("X-Internal-Secret", "s3cret")
                        .contentType(MediaType.APPLICATION_JSON).content(SETUP_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_024"));
    }

    @Test
    void reissue_시크릿이_틀리면_403_정상이면_204() throws Exception {
        mvc("s3cret").perform(post("/internal/shop-auth/accounts/10/password-setup-reissue")
                        .header("X-Internal-Secret", "wrong"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(managedUseCase);

        mvc("s3cret").perform(post("/internal/shop-auth/accounts/10/password-setup-reissue")
                        .header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isNoContent());
        verify(managedUseCase).reissuePasswordSetup(10L);
    }

    private static final String ONURIM_BODY = "{\"adminId\":1,\"companyId\":7,\"desiredType\":\"BOTH\","
            + "\"companyPhone\":\"02-123-4567\",\"address\":\"서울시 강남구\"}";

    private static OnurimApplicationResult onurimResult() {
        return new OnurimApplicationResult(50L, 7L, "꽃집", 1L, "홍길동", CompanyType.BOTH,
                OnurimApplicationStatus.PENDING, "02-123-4567", "서울시 강남구", null, null, null,
                null, null, null, null, List.of(), null, null, null, null, null, null);
    }

    private static MockMultipartFile data(String json) {
        return new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes(StandardCharsets.UTF_8));
    }

    private static MockMultipartFile doc(String part, String name) {
        return new MockMultipartFile(part, name, "image/png", new byte[]{1, 2, 3});
    }

    @Test
    void 온우림_이용신청_시크릿이_틀리면_403() throws Exception {
        mvc("s3cret").perform(multipart("/internal/shop-auth/onurim-applications")
                        .file(data(ONURIM_BODY))
                        .header("X-Internal-Secret", "wrong"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(onurimWithDocsUseCase);
    }

    @Test
    void 온우림_이용신청_서류없이_정상이면_결과를_반환() throws Exception {
        when(onurimWithDocsUseCase.apply(anyLong(), anyLong(), any(), any(), isNull(), isNull()))
                .thenReturn(onurimResult());

        mvc("s3cret").perform(multipart("/internal/shop-auth/onurim-applications")
                        .file(data(ONURIM_BODY))
                        .header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void 온우림_이용신청_서류_파트를_문서로_변환해_전달() throws Exception {
        when(onurimWithDocsUseCase.apply(anyLong(), anyLong(), any(), any(), any(), any()))
                .thenReturn(onurimResult());

        mvc("s3cret").perform(multipart("/internal/shop-auth/onurim-applications")
                        .file(data(ONURIM_BODY))
                        .file(doc("businessDoc", "biz.png"))
                        .file(doc("bankDoc", "bank.png"))
                        .header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isOk());

        ArgumentCaptor<SignupDocument> biz = ArgumentCaptor.forClass(SignupDocument.class);
        ArgumentCaptor<SignupDocument> bank = ArgumentCaptor.forClass(SignupDocument.class);
        verify(onurimWithDocsUseCase).apply(eq(1L), eq(7L), eq(CompanyType.BOTH), any(), biz.capture(), bank.capture());
        assertThat(biz.getValue().fileName()).isEqualTo("biz.png");
        assertThat(bank.getValue().fileName()).isEqualTo("bank.png");
        assertThat(biz.getValue().content()).containsExactly(1, 2, 3);
    }

    @Test
    void 온우림_이용신청_필수서류_누락은_400으로_매핑() throws Exception {
        when(onurimWithDocsUseCase.apply(anyLong(), anyLong(), any(), any(), any(), any()))
                .thenThrow(new BusinessException(OnurimApplicationErrorCode.DOCUMENT_REQUIRED));

        mvc("s3cret").perform(multipart("/internal/shop-auth/onurim-applications")
                        .file(data(ONURIM_BODY))
                        .header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ONURIM_APP_009"));
    }

    @Test
    void 온우림_이용신청_필수값이_없으면_400() throws Exception {
        mvc("s3cret").perform(multipart("/internal/shop-auth/onurim-applications")
                        .file(data("{\"adminId\":1,\"companyId\":7,\"desiredType\":\"BOTH\"}"))
                        .header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(onurimWithDocsUseCase);
    }

    @Test
    void 온우림_이용신청_data_파트가_없으면_400() throws Exception {
        mvc("s3cret").perform(multipart("/internal/shop-auth/onurim-applications")
                        .file(doc("businessDoc", "biz.png"))
                        .header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(onurimWithDocsUseCase);
    }

    @Test
    void 온우림_이용신청_중복은_409로_매핑() throws Exception {
        when(onurimWithDocsUseCase.apply(anyLong(), anyLong(), any(), any(), any(), any()))
                .thenThrow(new BusinessException(OnurimApplicationErrorCode.ALREADY_PENDING));

        mvc("s3cret").perform(multipart("/internal/shop-auth/onurim-applications")
                        .file(data(ONURIM_BODY))
                        .header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ONURIM_APP_003"));
    }

    @Test
    void 온우림_필요서류_조회는_시크릿_검증후_결과를_반환() throws Exception {
        when(onurimUseCase.getDocumentRequirement(1L, 7L)).thenReturn(new OnurimDocumentRequirement(true, false));

        mvc("s3cret").perform(get("/internal/shop-auth/onurim-applications/documents")
                        .param("adminId", "1").param("companyId", "7").header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needsBusinessReg").value(true))
                .andExpect(jsonPath("$.needsBankBook").value(false));

        mvc("s3cret").perform(get("/internal/shop-auth/onurim-applications/documents")
                        .param("adminId", "1").param("companyId", "7").header("X-Internal-Secret", "wrong"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 온우림_최신_신청_조회는_없으면_204_있으면_200() throws Exception {
        when(onurimUseCase.getLatest(7L)).thenReturn(java.util.Optional.empty());
        mvc("s3cret").perform(get("/internal/shop-auth/onurim-applications/latest")
                        .param("companyId", "7").header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isNoContent());

        when(onurimUseCase.getLatest(7L)).thenReturn(java.util.Optional.of(onurimResult()));
        mvc("s3cret").perform(get("/internal/shop-auth/onurim-applications/latest")
                        .param("companyId", "7").header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(50));

        mvc("s3cret").perform(get("/internal/shop-auth/onurim-applications/latest")
                        .param("companyId", "7").header("X-Internal-Secret", "wrong"))
                .andExpect(status().isForbidden());
    }
}
