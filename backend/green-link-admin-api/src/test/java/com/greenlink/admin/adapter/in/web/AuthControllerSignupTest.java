package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.global.exception.GlobalExceptionHandler;
import com.greenlink.core.application.auth.usecase.AdminLoginUseCase;
import com.greenlink.core.application.auth.usecase.FindIdByPhoneUseCase;
import com.greenlink.core.application.auth.usecase.ResetPasswordUseCase;
import com.greenlink.core.application.auth.usecase.SendPhoneVerificationUseCase;
import com.greenlink.core.application.auth.usecase.VendorLoginUseCase;
import com.greenlink.core.application.auth.usecase.VerifyPhoneCodeUseCase;
import com.greenlink.core.application.vendor.dto.SignupDocument;
import com.greenlink.core.application.vendor.dto.VendorResult;
import com.greenlink.core.application.vendor.dto.VendorSignupCommand;
import com.greenlink.core.application.vendor.dto.VendorSignupWithDocumentsResult;
import com.greenlink.core.application.vendor.usecase.VendorSignupWithDocumentsUseCase;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerSignupTest {

    private static final String DATA = "{\"loginId\":\"vendor01\",\"password\":\"Passw0rd!\",\"name\":\"홍길동\","
            + "\"phone\":\"010-1234-5678\",\"companyName\":\"꽃집\",\"roles\":[\"VENDOR\"]}";

    private final VendorSignupWithDocumentsUseCase signupUseCase = mock(VendorSignupWithDocumentsUseCase.class);

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new AuthController(
                    mock(AdminLoginUseCase.class), mock(VendorLoginUseCase.class), signupUseCase,
                    mock(SendPhoneVerificationUseCase.class), mock(VerifyPhoneCodeUseCase.class),
                    mock(FindIdByPhoneUseCase.class), mock(ResetPasswordUseCase.class)))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static MockMultipartFile json(String name, String body) {
        return new MockMultipartFile(name, "", "application/json", body.getBytes());
    }

    private static MockMultipartFile file(String name, String fileName) {
        return new MockMultipartFile(name, fileName, "image/png", new byte[]{1, 2, 3});
    }

    private VendorResult vendor() {
        return new VendorResult(10L, "꽃집", null, null, null, "홍길동", "010-1234-5678", null, null, null,
                "vendor01", "PENDING", null, "VENDOR", List.of(), List.of(), null, null, null, null, null, null);
    }

    @Test
    void 가입_정보와_서류_두_건을_함께_받아_201() throws Exception {
        when(signupUseCase.signup(any(), any(), any()))
                .thenReturn(new VendorSignupWithDocumentsResult(vendor(), List.of("bankBook")));

        mvc.perform(multipart("/api/auth/signup")
                        .file(json("data", DATA))
                        .file(file("businessDoc", "biz.png"))
                        .file(file("bankDoc", "bank.png")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendor.id").value(10))
                .andExpect(jsonPath("$.failedDocuments[0]").value("bankBook"));

        ArgumentCaptor<VendorSignupCommand> cmd = ArgumentCaptor.forClass(VendorSignupCommand.class);
        ArgumentCaptor<SignupDocument> biz = ArgumentCaptor.forClass(SignupDocument.class);
        ArgumentCaptor<SignupDocument> bank = ArgumentCaptor.forClass(SignupDocument.class);
        verify(signupUseCase).signup(cmd.capture(), biz.capture(), bank.capture());
        assertThat(cmd.getValue().loginId()).isEqualTo("vendor01");
        assertThat(biz.getValue().fileName()).isEqualTo("biz.png");
        assertThat(bank.getValue().fileName()).isEqualTo("bank.png");
        assertThat(bank.getValue().content()).containsExactly(1, 2, 3);
    }

    @Test
    void 서류_파트가_없으면_400() throws Exception {
        mvc.perform(multipart("/api/auth/signup")
                        .file(json("data", DATA))
                        .file(file("businessDoc", "biz.png")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAM"));
        verifyNoInteractions(signupUseCase);
    }

    @Test
    void 가입_정보_필수값이_비면_400() throws Exception {
        String invalid = "{\"loginId\":\"\",\"password\":\"Passw0rd!\",\"name\":\"홍길동\","
                + "\"phone\":\"010-1234-5678\",\"companyName\":\"꽃집\"}";

        mvc.perform(multipart("/api/auth/signup")
                        .file(json("data", invalid))
                        .file(file("businessDoc", "biz.png"))
                        .file(file("bankDoc", "bank.png")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAM"));
        verifyNoInteractions(signupUseCase);
    }

    @Test
    void 서류_검증_실패는_업무_에러코드로_400() throws Exception {
        when(signupUseCase.signup(any(), any(), any()))
                .thenThrow(new BusinessException(VendorErrorCode.DOCUMENT_INVALID_TYPE));

        mvc.perform(multipart("/api/auth/signup")
                        .file(json("data", DATA))
                        .file(file("businessDoc", "biz.exe"))
                        .file(file("bankDoc", "bank.png")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VENDOR_006"));
    }
}
