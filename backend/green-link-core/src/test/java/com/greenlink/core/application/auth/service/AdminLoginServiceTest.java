package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.AdminLoginCommand;
import com.greenlink.core.application.auth.dto.AdminLoginResult;
import com.greenlink.core.application.auth.port.TokenProvider;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyStatus;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AdminLoginServiceTest {

    @Mock AdminRepository adminRepository;
    @Mock CompanyRepository companyRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TokenProvider tokenProvider;

    private AdminLoginService service;

    @BeforeEach
    void setUp() {
        service = new AdminLoginService(adminRepository, companyRepository, passwordEncoder, tokenProvider);
    }

    private Admin activeAdmin() {
        Admin admin = Admin.of(1L, "admin01", "hashedPw", "관리자", "010-0000-0000", null, null, AdminStatus.ACTIVE, null, null);
        admin.restoreAccess(AdminRole.SUPER_ADMIN, true);
        return admin;
    }

    private Admin vendorAdmin(boolean onurimEnabled) {
        Admin admin = Admin.of(4L, "vendor03", "hashedPw", "홍길동", "010-5555-6666", null, 1L, AdminStatus.ACTIVE, null, null);
        admin.restoreAccess(AdminRole.PARTNER_ADMIN, onurimEnabled);
        return admin;
    }

    private Company companyOf(CompanyType type, boolean onurimEnabled, boolean siteEnabled) {
        Company company = Company.of(1L, null, "테스트업체", null, null, null, null, null, type, CompanyStatus.ACTIVE, true,
                null, null, null, null, null, null);
        company.restoreServiceFlags(onurimEnabled, siteEnabled);
        return company;
    }

    private void givenCredentialsOk(Admin admin) {
        given(adminRepository.findByLoginId(admin.getLoginId())).willReturn(Optional.of(admin));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
    }

    private void assertLoginFails(Admin admin, AuthErrorCode expected) {
        givenCredentialsOk(admin);
        assertThatThrownBy(() -> service.login(new AdminLoginCommand(admin.getLoginId(), "rawPw")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(expected));
        verifyNoInteractions(tokenProvider);
    }

    private Company vendorCompany() {
        return Company.of(1L, null, "테스트업체", null, null, null, null, null, CompanyType.VENDOR, CompanyStatus.ACTIVE, true,
                null, null, null, null, null, null);
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(activeAdmin()));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(tokenProvider.generateToken(anyLong(), eq(AdminRole.SUPER_ADMIN), isNull(), isNull())).willReturn("jwt-token");

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
        Admin pending = Admin.of(2L, "vendor01", "hashedPw", "홍길동", "010-1111-2222", null, 1L, AdminStatus.PENDING, null, null);
        given(adminRepository.findByLoginId("vendor01")).willReturn(Optional.of(pending));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);

        assertThatThrownBy(() -> service.login(new AdminLoginCommand("vendor01", "rawPw")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.ACCOUNT_PENDING));
    }

    @Test
    void login_withInactiveAccount_throwsAccountInactive() {
        Admin inactive = Admin.of(3L, "vendor02", "hashedPw", "김철수", "010-3333-4444", null, 1L, AdminStatus.INACTIVE, null, null);
        given(adminRepository.findByLoginId("vendor02")).willReturn(Optional.of(inactive));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);

        assertThatThrownBy(() -> service.login(new AdminLoginCommand("vendor02", "rawPw")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.ACCOUNT_INACTIVE));
    }

    @Test
    void login_asVendor_generatesVendorRoleToken() {
        Admin vendor = Admin.of(4L, "vendor03", "hashedPw", "홍길동", "010-5555-6666", null, 1L, AdminStatus.ACTIVE, null, null);
        given(adminRepository.findByLoginId("vendor03")).willReturn(Optional.of(vendor));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(companyRepository.findById(1L)).willReturn(Optional.of(vendorCompany()));
        given(tokenProvider.generateToken(eq(4L), eq(AdminRole.PARTNER_ADMIN), eq(CompanyType.VENDOR), eq(1L))).willReturn("vendor-token");

        AdminLoginResult result = service.login(new AdminLoginCommand("vendor03", "rawPw"));

        assertThat(result.accessToken()).isEqualTo("vendor-token");
    }

    @Test
    void login_asSuperAdmin_generatesSuperAdminRoleToken() {
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(activeAdmin()));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(tokenProvider.generateToken(eq(1L), eq(AdminRole.SUPER_ADMIN), isNull(), isNull())).willReturn("admin-token");

        AdminLoginResult result = service.login(new AdminLoginCommand("admin01", "rawPw"));

        assertThat(result.accessToken()).isEqualTo("admin-token");
    }

    @Test
    void login_회사없는_일반관리자는_슈퍼관리자가_아니므로_차단() {
        Admin noCompany = Admin.of(5L, "shop01", "hashedPw", "샵가입", "010-0000-0000", null, null, AdminStatus.ACTIVE, null, null);
        noCompany.restoreAccess(AdminRole.PARTNER_ADMIN, true);
        assertLoginFails(noCompany, AuthErrorCode.ONURIM_NOT_ENABLED);
    }

    @Test
    void login_회사를_찾을수없으면_슈퍼관리자로_폴백하지않고_차단() {
        Admin admin = vendorAdmin(true);
        given(companyRepository.findById(1L)).willReturn(Optional.empty());
        assertLoginFails(admin, AuthErrorCode.ONURIM_NOT_ENABLED);
    }

    @Test
    void login_회사_온우림사용이_꺼져있으면_차단() {
        Admin admin = vendorAdmin(true);
        given(companyRepository.findById(1L)).willReturn(Optional.of(companyOf(CompanyType.VENDOR, false, true)));
        assertLoginFails(admin, AuthErrorCode.ONURIM_NOT_ENABLED);
    }

    @Test
    void login_회사_타입이_없으면_차단() {
        Admin admin = vendorAdmin(true);
        given(companyRepository.findById(1L)).willReturn(Optional.of(companyOf(null, true, true)));
        assertLoginFails(admin, AuthErrorCode.ONURIM_NOT_ENABLED);
    }

    @Test
    void login_관리자_온우림사용이_꺼져있으면_차단() {
        Admin admin = vendorAdmin(false);
        given(companyRepository.findById(1L)).willReturn(Optional.of(companyOf(CompanyType.VENDOR, true, true)));
        assertLoginFails(admin, AuthErrorCode.ONURIM_NOT_ENABLED);
    }

    @Test
    void login_슈퍼관리자는_회사_플래그와_무관하게_허용() {
        Admin superAdmin = activeAdmin();
        superAdmin.restoreAccess(AdminRole.SUPER_ADMIN, false);
        givenCredentialsOk(superAdmin);
        given(tokenProvider.generateToken(eq(1L), eq(AdminRole.SUPER_ADMIN), isNull(), isNull())).willReturn("admin-token");

        AdminLoginResult result = service.login(new AdminLoginCommand("admin01", "rawPw"));

        assertThat(result.accessToken()).isEqualTo("admin-token");
        assertThat(result.companyName()).isNull();
        assertThat(result.role()).isEqualTo("SUPER_ADMIN");
        assertThat(result.companyType()).isNull();
    }

    @Test
    void login_발주사는_ORDERING_회사유형_토큰() {
        Admin admin = vendorAdmin(true);
        givenCredentialsOk(admin);
        given(companyRepository.findById(1L)).willReturn(Optional.of(companyOf(CompanyType.ORDERING, true, false)));
        given(tokenProvider.generateToken(eq(4L), eq(AdminRole.PARTNER_ADMIN), eq(CompanyType.ORDERING), eq(1L))).willReturn("ordering-token");

        AdminLoginResult result = service.login(new AdminLoginCommand("vendor03", "rawPw"));

        assertThat(result.accessToken()).isEqualTo("ordering-token");
        assertThat(result.vendorId()).isEqualTo(1L);
        assertThat(result.companyName()).isEqualTo("테스트업체");
        assertThat(result.role()).isEqualTo("PARTNER_ADMIN");
        assertThat(result.companyType()).isEqualTo("ORDERING");
    }

    @Test
    void login_수발주사는_BOTH_회사유형_토큰() {
        Admin admin = vendorAdmin(true);
        givenCredentialsOk(admin);
        given(companyRepository.findById(1L)).willReturn(Optional.of(companyOf(CompanyType.BOTH, true, true)));
        given(tokenProvider.generateToken(eq(4L), eq(AdminRole.PARTNER_ADMIN), eq(CompanyType.BOTH), eq(1L))).willReturn("both-token");

        AdminLoginResult result = service.login(new AdminLoginCommand("vendor03", "rawPw"));

        assertThat(result.accessToken()).isEqualTo("both-token");
        assertThat(result.companyType()).isEqualTo("BOTH");
    }
}
