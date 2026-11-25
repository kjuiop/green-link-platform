package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.VendorLoginCommand;
import com.greenlink.core.application.auth.dto.VendorLoginResult;
import com.greenlink.core.application.auth.port.TokenProvider;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.BusinessStatus;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class VendorLoginServiceTest {

    @Mock AdminRepository adminRepository;
    @Mock CompanyRepository companyRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TokenProvider tokenProvider;

    private VendorLoginService service;

    @BeforeEach
    void setUp() {
        service = new VendorLoginService(adminRepository, companyRepository, passwordEncoder, tokenProvider);
    }

    private Admin vendorAdmin(boolean onurimEnabled) {
        Admin admin = Admin.of(4L, "vendor03", "hashedPw", "홍길동", "010-5555-6666", null, 1L, AdminStatus.ACTIVE, null, null);
        admin.restoreAccessFlags(false, onurimEnabled);
        return admin;
    }

    private Company company(CompanyType type, boolean onurimEnabled) {
        Company company = Company.of(1L, null, "테스트업체", null, null, null, null, null, type, CompanyStatus.ACTIVE, true,
                null, null, null, null, null, BusinessStatus.OPEN);
        company.restoreServiceFlags(onurimEnabled, true);
        return company;
    }

    private void givenCredentialsOk(Admin admin) {
        given(adminRepository.findByLoginId("vendor03")).willReturn(Optional.of(admin));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
    }

    private void assertBlocked() {
        assertThatThrownBy(() -> service.login(new VendorLoginCommand("vendor03", "rawPw")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(AuthErrorCode.ONURIM_NOT_ENABLED));
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void login_온우림_사용_회사와_관리자는_토큰_발급() {
        givenCredentialsOk(vendorAdmin(true));
        given(companyRepository.findById(1L)).willReturn(Optional.of(company(CompanyType.VENDOR, true)));
        given(tokenProvider.generateToken(4L, List.of("VENDOR"), 1L)).willReturn("vendor-token");

        VendorLoginResult result = service.login(new VendorLoginCommand("vendor03", "rawPw"));

        assertThat(result.accessToken()).isEqualTo("vendor-token");
    }

    @Test
    void login_회사_온우림사용이_꺼져있으면_차단() {
        givenCredentialsOk(vendorAdmin(true));
        given(companyRepository.findById(1L)).willReturn(Optional.of(company(CompanyType.VENDOR, false)));
        assertBlocked();
    }

    @Test
    void login_회사_타입이_없으면_차단() {
        givenCredentialsOk(vendorAdmin(true));
        given(companyRepository.findById(1L)).willReturn(Optional.of(company(null, true)));
        assertBlocked();
    }

    @Test
    void login_회사를_찾을수없으면_차단() {
        givenCredentialsOk(vendorAdmin(true));
        given(companyRepository.findById(1L)).willReturn(Optional.empty());
        assertBlocked();
    }

    @Test
    void login_관리자_온우림사용이_꺼져있으면_차단() {
        givenCredentialsOk(vendorAdmin(false));
        given(companyRepository.findById(1L)).willReturn(Optional.of(company(CompanyType.VENDOR, true)));
        assertBlocked();
    }
}
