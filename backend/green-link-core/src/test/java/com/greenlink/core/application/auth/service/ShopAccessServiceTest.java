package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.CompanySummary;
import com.greenlink.core.application.auth.dto.VerifiedAccount;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShopAccessServiceTest {

    @Mock AdminRepository adminRepository;
    @Mock CompanyRepository companyRepository;

    private ShopAccessService service;

    @BeforeEach
    void setUp() {
        service = new ShopAccessService(adminRepository, companyRepository);
    }

    private Company company(boolean siteEnabled) {
        Company c = Company.of(7L, "123-45-67890", "기존업체", null, null, null, null, null,
            CompanyType.VENDOR, CompanyStatus.ACTIVE, true, null, null, null, null, null, null);
        c.restoreServiceFlags(true, siteEnabled);
        return c;
    }

    private Admin admin(Long companyId, AdminStatus status, String deletedAt) {
        Admin a = Admin.of(1L, "shop01", "pw", "홍길동", "010-1234-5678", null, companyId, status, null, deletedAt);
        a.restoreAccess(AdminRole.PARTNER_ADMIN, companyId != null);
        return a;
    }

    private void assertCode(Runnable call, AuthErrorCode expected) {
        assertThatThrownBy(call::run)
            .isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    private void stubSaves() {
        given(companyRepository.save(any(Company.class))).willAnswer(inv -> inv.getArgument(0));
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void 사이트가_꺼진_회사는_켜고_소속없는_계정을_연결한다() {
        given(adminRepository.findById(1L)).willReturn(Optional.of(admin(null, AdminStatus.ACTIVE, null)));
        given(companyRepository.findById(7L)).willReturn(Optional.of(company(false)));
        stubSaves();

        VerifiedAccount result = service.grant(1L, 7L, AdminRole.PARTNER_ADMIN);

        assertThat(result.companyId()).isEqualTo(7L);
        assertThat(result.companyName()).isEqualTo("기존업체");
        assertThat(result.companySiteEnabled()).isTrue();
        assertThat(result.adminOnurimEnabled()).isFalse();
        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(companyCaptor.capture());
        assertThat(companyCaptor.getValue().isSiteEnabled()).isTrue();
        ArgumentCaptor<Admin> adminCaptor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(adminCaptor.capture());
        assertThat(adminCaptor.getValue().getCompanyId()).isEqualTo(7L);
        assertThat(adminCaptor.getValue().getRole()).isEqualTo(AdminRole.PARTNER_ADMIN);
    }

    @Test
    void 승인된_role로_온우림_role이_바뀐다() {
        given(adminRepository.findById(1L)).willReturn(Optional.of(admin(null, AdminStatus.ACTIVE, null)));
        given(companyRepository.findById(7L)).willReturn(Optional.of(company(true)));
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> inv.getArgument(0));

        VerifiedAccount result = service.grant(1L, 7L, AdminRole.BRAND_ADMIN);

        assertThat(result.role()).isEqualTo("BRAND_ADMIN");
        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(AdminRole.BRAND_ADMIN);
    }

    @Test
    void 슈퍼관리자_role로는_승인할_수_없다() {
        assertCode(() -> service.grant(1L, 7L, AdminRole.SUPER_ADMIN), AuthErrorCode.SHOP_SIGNUP_ROLE_INVALID);
        assertCode(() -> service.grant(1L, 7L, null), AuthErrorCode.SHOP_SIGNUP_ROLE_INVALID);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void 슈퍼관리자_계정에는_승인을_적용하지_않는다() {
        Admin superAdmin = Admin.of(1L, "root", "pw", null, null, null, null, AdminStatus.ACTIVE, null, null);
        superAdmin.restoreAccess(AdminRole.SUPER_ADMIN, true);
        given(adminRepository.findById(1L)).willReturn(Optional.of(superAdmin));

        assertCode(() -> service.grant(1L, 7L, AdminRole.PARTNER_ADMIN), AuthErrorCode.SHOP_SIGNUP_ROLE_INVALID);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void changeRole은_온우림_계정의_role을_바꾼다() {
        given(adminRepository.findById(1L)).willReturn(Optional.of(admin(7L, AdminStatus.ACTIVE, null)));
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> inv.getArgument(0));

        service.changeRole(1L, AdminRole.BRAND_ADMIN);

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(AdminRole.BRAND_ADMIN);
    }

    @Test
    void changeRole은_슈퍼관리자_role_변경과_슈퍼관리자_계정을_거부한다() {
        Admin superAdmin = Admin.of(2L, "root", "pw", null, null, null, null, AdminStatus.ACTIVE, null, null);
        superAdmin.restoreAccess(AdminRole.SUPER_ADMIN, true);
        given(adminRepository.findById(2L)).willReturn(Optional.of(superAdmin));

        assertCode(() -> service.changeRole(1L, AdminRole.SUPER_ADMIN), AuthErrorCode.SHOP_SIGNUP_ROLE_INVALID);
        assertCode(() -> service.changeRole(2L, AdminRole.PARTNER_ADMIN), AuthErrorCode.SHOP_SIGNUP_ROLE_INVALID);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void changeRole은_없는_계정이면_404() {
        given(adminRepository.findById(1L)).willReturn(Optional.empty());

        assertCode(() -> service.changeRole(1L, AdminRole.BRAND_ADMIN), AuthErrorCode.SHOP_ACCESS_ADMIN_NOT_FOUND);
    }

    @Test
    void 이미_사이트가_켜져있고_같은_회사_소속이면_회사는_저장하지_않는다() {
        given(adminRepository.findById(1L)).willReturn(Optional.of(admin(7L, AdminStatus.ACTIVE, null)));
        given(companyRepository.findById(7L)).willReturn(Optional.of(company(true)));
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> inv.getArgument(0));

        VerifiedAccount result = service.grant(1L, 7L, AdminRole.PARTNER_ADMIN);

        assertThat(result.companySiteEnabled()).isTrue();
        assertThat(result.companyId()).isEqualTo(7L);
        verify(companyRepository, never()).save(any());
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void 사이트가_꺼진_회사의_같은_회사_계정은_회사를_켜고_role을_반영한다() {
        given(adminRepository.findById(1L)).willReturn(Optional.of(admin(7L, AdminStatus.ACTIVE, null)));
        given(companyRepository.findById(7L)).willReturn(Optional.of(company(false)));
        stubSaves();

        VerifiedAccount result = service.grant(1L, 7L, AdminRole.PARTNER_ADMIN);

        assertThat(result.companySiteEnabled()).isTrue();
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void 반복_호출해도_결과가_같다() {
        Admin applicant = admin(null, AdminStatus.ACTIVE, null);
        Company target = company(false);
        given(adminRepository.findById(1L)).willReturn(Optional.of(applicant));
        given(companyRepository.findById(7L)).willReturn(Optional.of(target));
        stubSaves();

        VerifiedAccount first = service.grant(1L, 7L, AdminRole.PARTNER_ADMIN);
        VerifiedAccount second = service.grant(1L, 7L, AdminRole.PARTNER_ADMIN);

        assertThat(second).isEqualTo(first);
    }

    @Test
    void 다른_회사에_소속된_계정이면_409_이고_아무것도_바꾸지_않는다() {
        given(adminRepository.findById(1L)).willReturn(Optional.of(admin(9L, AdminStatus.ACTIVE, null)));
        given(companyRepository.findById(7L)).willReturn(Optional.of(company(false)));

        assertCode(() -> service.grant(1L, 7L, AdminRole.PARTNER_ADMIN), AuthErrorCode.SHOP_ACCESS_COMPANY_MISMATCH);
        verify(companyRepository, never()).save(any());
        verify(adminRepository, never()).save(any());
    }

    @Test
    void 없는_회사면_404() {
        given(adminRepository.findById(1L)).willReturn(Optional.of(admin(null, AdminStatus.ACTIVE, null)));
        given(companyRepository.findById(7L)).willReturn(Optional.empty());

        assertCode(() -> service.grant(1L, 7L, AdminRole.PARTNER_ADMIN), AuthErrorCode.SHOP_ACCESS_COMPANY_NOT_FOUND);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void 없는_계정이거나_삭제된_계정이면_404() {
        given(adminRepository.findById(1L)).willReturn(Optional.empty());
        given(adminRepository.findById(2L))
            .willReturn(Optional.of(admin(null, AdminStatus.INACTIVE, "2026-11-01 00:00:00")));

        assertCode(() -> service.grant(1L, 7L, AdminRole.PARTNER_ADMIN), AuthErrorCode.SHOP_ACCESS_ADMIN_NOT_FOUND);
        assertCode(() -> service.grant(2L, 7L, AdminRole.PARTNER_ADMIN), AuthErrorCode.SHOP_ACCESS_ADMIN_NOT_FOUND);
    }

    @Test
    void 정지되거나_승인대기인_계정은_거부한다() {
        given(adminRepository.findById(1L)).willReturn(Optional.of(admin(null, AdminStatus.INACTIVE, null)));
        given(adminRepository.findById(2L)).willReturn(Optional.of(admin(null, AdminStatus.PENDING, null)));

        assertCode(() -> service.grant(1L, 7L, AdminRole.PARTNER_ADMIN), AuthErrorCode.ACCOUNT_INACTIVE);
        assertCode(() -> service.grant(2L, 7L, AdminRole.PARTNER_ADMIN), AuthErrorCode.ACCOUNT_PENDING);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void 회사_요약은_id와_회사명을_반환한다() {
        given(companyRepository.findAllByIdIn(List.of(7L))).willReturn(List.of(company(false)));

        assertThat(service.getCompanySummaries(List.of(7L)))
            .containsExactly(new CompanySummary(7L, "기존업체"));
    }

    @Test
    void 회사_요약은_빈_입력이면_조회하지_않는다() {
        assertThat(service.getCompanySummaries(List.of())).isEmpty();
        assertThat(service.getCompanySummaries(null)).isEmpty();
        verify(companyRepository, never()).findAllByIdIn(any());
    }
}
