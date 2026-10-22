package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.AdminLoginCommand;
import com.greenlink.core.application.auth.dto.AdminLoginResult;
import com.greenlink.core.application.auth.port.TokenProvider;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminLoginServiceTest {

    @Mock AdminRepository adminRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TokenProvider tokenProvider;

    private AdminLoginService service;

    @BeforeEach
    void setUp() {
        service = new AdminLoginService(adminRepository, passwordEncoder, tokenProvider);
    }

    private Admin activeAdmin() {
        return Admin.of(1L, "admin01", "hashedPw", "관리자", "010-0000-0000", null, AdminStatus.ACTIVE, null);
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(activeAdmin()));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(tokenProvider.generateToken(anyLong(), any(List.class))).willReturn("jwt-token");

        AdminLoginResult result = service.login(new AdminLoginCommand("admin01", "rawPw"));

        assertThat(result.accessToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_withUnknownLoginId_throwsInvalidCredentials() {
        given(adminRepository.findByLoginId(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new AdminLoginCommand("unknown", "pw")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void login_withWrongPassword_throwsInvalidCredentials() {
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(activeAdmin()));
        given(passwordEncoder.matches("wrongPw", "hashedPw")).willReturn(false);

        assertThatThrownBy(() -> service.login(new AdminLoginCommand("admin01", "wrongPw")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void login_withPendingAccount_throwsAccountPending() {
        Admin pending = Admin.of(2L, "vendor01", "hashedPw", "홍길동", "010-1111-2222", 1L, AdminStatus.PENDING, null);
        given(adminRepository.findByLoginId("vendor01")).willReturn(Optional.of(pending));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);

        assertThatThrownBy(() -> service.login(new AdminLoginCommand("vendor01", "rawPw")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.ACCOUNT_PENDING));
    }

    @Test
    void login_withInactiveAccount_throwsAccountInactive() {
        Admin inactive = Admin.of(3L, "vendor02", "hashedPw", "김철수", "010-3333-4444", 1L, AdminStatus.INACTIVE, null);
        given(adminRepository.findByLoginId("vendor02")).willReturn(Optional.of(inactive));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);

        assertThatThrownBy(() -> service.login(new AdminLoginCommand("vendor02", "rawPw")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.ACCOUNT_INACTIVE));
    }
}
