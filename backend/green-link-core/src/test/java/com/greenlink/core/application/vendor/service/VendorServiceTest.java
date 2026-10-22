package com.greenlink.core.application.vendor.service;

import com.greenlink.core.application.vendor.dto.CreateVendorCommand;
import com.greenlink.core.application.vendor.dto.VendorResult;
import com.greenlink.core.application.vendor.dto.VendorSignupCommand;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyStatus;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VendorServiceTest {

    @Mock AdminRepository adminRepository;
    @Mock CompanyRepository companyRepository;
    @Mock PasswordEncoder passwordEncoder;

    private VendorService service;

    @BeforeEach
    void setUp() {
        service = new VendorService(adminRepository, companyRepository, passwordEncoder);
    }

    private Company savedCompany(Long id) {
        return Company.of(id, "123-45-67890", "테스트꽃집", "홍대표", "02-1234-5678",
                "서울시 강남구", "101호", CompanyType.VENDOR, CompanyStatus.ACTIVE);
    }

    private Admin savedAdmin(Long id, Long companyId, AdminStatus status) {
        return Admin.of(id, "vendor01", "encodedPw", "홍길동", "010-1234-5678",
                companyId, status, "2026-10-22T10:00:00");
    }

    // ── createVendor ──────────────────────────────────────────────────────────

    @Test
    void createVendor_duplicateLoginId_throwsDuplicateLoginId() {
        given(adminRepository.existsByLoginId("vendor01")).willReturn(true);

        assertThatThrownBy(() -> service.createVendor(
                new CreateVendorCommand("테스트꽃집", "123-45-67890", "서울", "", "홍길동", "010-0000-0000", "vendor01", "pw")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(VendorErrorCode.DUPLICATE_LOGIN_ID));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void createVendor_duplicateBusinessNo_throwsDuplicateBusinessNo() {
        given(adminRepository.existsByLoginId("vendor01")).willReturn(false);
        given(companyRepository.existsByBusinessNo("123-45-67890")).willReturn(true);

        assertThatThrownBy(() -> service.createVendor(
                new CreateVendorCommand("테스트꽃집", "123-45-67890", "서울", "", "홍길동", "010-0000-0000", "vendor01", "pw")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(VendorErrorCode.DUPLICATE_BUSINESS_NO));
    }

    @Test
    void createVendor_success_createsCompanyAndAdmin() {
        given(adminRepository.existsByLoginId("vendor01")).willReturn(false);
        given(companyRepository.existsByBusinessNo(anyString())).willReturn(false);
        given(passwordEncoder.encode("pw")).willReturn("encodedPw");
        given(companyRepository.save(any(Company.class))).willReturn(savedCompany(1L));
        given(adminRepository.save(any(Admin.class))).willReturn(savedAdmin(2L, 1L, AdminStatus.ACTIVE));

        VendorResult result = service.createVendor(
                new CreateVendorCommand("테스트꽃집", "123-45-67890", "서울", "", "홍길동", "010-0000-0000", "vendor01", "pw"));

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.companyName()).isEqualTo("테스트꽃집");
        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    // ── signup ────────────────────────────────────────────────────────────────

    @Test
    void signup_duplicateLoginId_throwsDuplicateLoginId() {
        given(adminRepository.existsByLoginId("vendor01")).willReturn(true);

        assertThatThrownBy(() -> service.signup(
                new VendorSignupCommand("vendor01", "pw", "홍길동", "010-0000-0000",
                        "123-45-67890", "테스트꽃집", "홍대표", "02-1234-5678", "서울", "")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(VendorErrorCode.DUPLICATE_LOGIN_ID));
    }

    @Test
    void signup_withExistingBusinessNo_linksToExistingCompany() {
        Company existing = savedCompany(10L);
        given(adminRepository.existsByLoginId("vendor01")).willReturn(false);
        given(passwordEncoder.encode("pw")).willReturn("encodedPw");
        given(companyRepository.existsByBusinessNo("123-45-67890")).willReturn(true);
        given(companyRepository.findByBusinessNo("123-45-67890")).willReturn(Optional.of(existing));
        given(adminRepository.save(any(Admin.class))).willReturn(savedAdmin(2L, 10L, AdminStatus.PENDING));

        VendorResult result = service.signup(
                new VendorSignupCommand("vendor01", "pw", "홍길동", "010-0000-0000",
                        "123-45-67890", "테스트꽃집", "홍대표", "02-1234-5678", "서울", ""));

        // 기존 company 연결 — 신규 company 저장 없음
        verify(companyRepository, never()).save(any());
        assertThat(result.status()).isEqualTo("PENDING");
    }

    @Test
    void signup_withNewBusinessNo_createsNewCompany() {
        given(adminRepository.existsByLoginId("vendor01")).willReturn(false);
        given(passwordEncoder.encode("pw")).willReturn("encodedPw");
        given(companyRepository.existsByBusinessNo("999-88-77777")).willReturn(false);
        given(companyRepository.save(any(Company.class))).willReturn(savedCompany(20L));
        given(adminRepository.save(any(Admin.class))).willReturn(savedAdmin(3L, 20L, AdminStatus.PENDING));

        VendorResult result = service.signup(
                new VendorSignupCommand("vendor01", "pw", "홍길동", "010-0000-0000",
                        "999-88-77777", "새꽃집", "김대표", "02-0000-0000", "부산", ""));

        verify(companyRepository).save(any(Company.class));
        assertThat(result.status()).isEqualTo("PENDING");
    }

    // ── updateVendorStatus ────────────────────────────────────────────────────

    @Test
    void updateVendorStatus_activate_setsActive() {
        Admin pending = savedAdmin(1L, 1L, AdminStatus.PENDING);
        given(adminRepository.findById(1L)).willReturn(Optional.of(pending));
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> inv.getArgument(0));
        given(companyRepository.findById(anyLong())).willReturn(Optional.of(savedCompany(1L)));

        VendorResult result = service.updateVendorStatus(1L, "activate");

        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    @Test
    void updateVendorStatus_suspend_setsInactive() {
        Admin active = savedAdmin(1L, 1L, AdminStatus.ACTIVE);
        given(adminRepository.findById(1L)).willReturn(Optional.of(active));
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> inv.getArgument(0));
        given(companyRepository.findById(anyLong())).willReturn(Optional.of(savedCompany(1L)));

        VendorResult result = service.updateVendorStatus(1L, "suspend");

        assertThat(result.status()).isEqualTo("INACTIVE");
    }

    @Test
    void updateVendorStatus_invalidAction_throwsIllegalArgumentException() {
        Admin active = savedAdmin(1L, 1L, AdminStatus.ACTIVE);
        given(adminRepository.findById(1L)).willReturn(Optional.of(active));

        assertThatThrownBy(() -> service.updateVendorStatus(1L, "invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
