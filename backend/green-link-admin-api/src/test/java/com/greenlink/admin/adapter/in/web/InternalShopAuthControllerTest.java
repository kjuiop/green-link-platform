package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.global.exception.GlobalExceptionHandler;
import com.greenlink.core.application.auth.dto.VerifiedAccount;
import com.greenlink.core.application.auth.usecase.VerifyAdminAccountUseCase;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
    private static final VerifiedAccount ACCOUNT = new VerifiedAccount(1L, "admin01", "관리자", "테스트업체", false);

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
                .andExpect(jsonPath("$.companyName").value("테스트업체"))
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
    void accounts_정지_계정은_403으로_매핑() throws Exception {
        when(useCase.getActiveAccount(anyLong()))
                .thenThrow(new BusinessException(AuthErrorCode.ACCOUNT_INACTIVE));

        mvc("s3cret").perform(get("/internal/shop-auth/accounts/1")
                        .header("X-Internal-Secret", "s3cret"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_006"));
    }
}
