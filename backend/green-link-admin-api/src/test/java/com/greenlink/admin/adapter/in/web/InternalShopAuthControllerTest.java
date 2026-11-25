package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.global.exception.GlobalExceptionHandler;
import com.greenlink.core.application.auth.dto.AccountSummary;
import com.greenlink.core.application.auth.dto.VerifiedAccount;
import com.greenlink.core.application.auth.usecase.VerifyAdminAccountUseCase;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InternalShopAuthControllerTest {

    private static final String BODY = "{\"loginId\":\"admin01\",\"password\":\"rawPw\"}";
    private static final VerifiedAccount ACCOUNT = new VerifiedAccount(1L, "admin01", "관리자", 7L, "테스트업체", true, false, true, false);

    private final VerifyAdminAccountUseCase useCase = mock(VerifyAdminAccountUseCase.class);

    private MockMvc mvc(String secret) {
        return MockMvcBuilders.standaloneSetup(new InternalShopAuthController(useCase, secret))
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
                new AccountSummary(1L, "admin01", "관리자", 7L, "테스트업체"),
                new AccountSummary(2L, "admin02", "직원", null, null)));

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
}
