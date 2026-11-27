package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.AccountSummary;
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
class VerifyAdminAccountServiceTest {

    @Mock AdminRepository adminRepository;
    @Mock CompanyRepository companyRepository;
    @Mock PasswordEncoder passwordEncoder;

    private VerifyAdminAccountService service;

    @BeforeEach
    void setUp() {
        service = new VerifyAdminAccountService(adminRepository, companyRepository, passwordEncoder);
    }

    private Admin admin(Long companyId, AdminStatus status, String deletedAt) {
        Admin admin = Admin.of(1L, "admin01", "hashedPw", "관리자", "010-0000-0000", null, companyId, status, null, deletedAt);
        admin.restoreAccess(companyId == null ? AdminRole.SUPER_ADMIN : AdminRole.PARTNER_ADMIN, true);
        return admin;
    }

    private Company company() {
        return Company.of(7L, null, "테스트업체", null, null, null, null, null, CompanyType.VENDOR, CompanyStatus.ACTIVE, true,
                null, null, null, null, null, null);
    }

    private void assertCode(Runnable call, AuthErrorCode expected) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void verify_일반계정은_회사명을_채우고_슈퍼관리자가_아니다() {
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(admin(7L, AdminStatus.ACTIVE, null)));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(companyRepository.findById(7L)).willReturn(Optional.of(company()));

        VerifiedAccount result = service.verify("admin01", "rawPw");

        assertThat(result.adminId()).isEqualTo(1L);
        assertThat(result.loginId()).isEqualTo("admin01");
        assertThat(result.name()).isEqualTo("관리자");
        assertThat(result.companyName()).isEqualTo("테스트업체");
        assertThat(result.role()).isEqualTo("PARTNER_ADMIN");
        assertThat(result.companyId()).isEqualTo(7L);
        assertThat(result.companyOnurimEnabled()).isTrue();
        assertThat(result.companySiteEnabled()).isFalse();
        assertThat(result.adminOnurimEnabled()).isTrue();
    }

    @Test
    void verify_슈퍼관리자_플래그는_회사_유무가_아니라_관리자_플래그를_따른다() {
        Admin noCompanyNotSuper = Admin.of(1L, "admin01", "hashedPw", "관리자", "010-0000-0000", null, null, AdminStatus.ACTIVE, null, null);
        noCompanyNotSuper.restoreAccess(AdminRole.PARTNER_ADMIN, false);
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(noCompanyNotSuper));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);

        VerifiedAccount result = service.verify("admin01", "rawPw");

        assertThat(result.role()).isEqualTo("PARTNER_ADMIN");
        assertThat(result.companyId()).isNull();
        assertThat(result.companyOnurimEnabled()).isFalse();
        assertThat(result.adminOnurimEnabled()).isFalse();
    }

    @Test
    void verify_회사_플래그가_응답에_반영된다() {
        Company siteOnly = company();
        siteOnly.restoreServiceFlags(false, true);
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(admin(7L, AdminStatus.ACTIVE, null)));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(companyRepository.findById(7L)).willReturn(Optional.of(siteOnly));

        VerifiedAccount result = service.verify("admin01", "rawPw");

        assertThat(result.companyOnurimEnabled()).isFalse();
        assertThat(result.companySiteEnabled()).isTrue();
    }

    @Test
    void verify_회사가_없는_계정은_슈퍼관리자다() {
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(admin(null, AdminStatus.ACTIVE, null)));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);

        VerifiedAccount result = service.verify("admin01", "rawPw");

        assertThat(result.role()).isEqualTo("SUPER_ADMIN");
        assertThat(result.companyName()).isNull();
    }

    @Test
    void verify_없는_아이디는_INVALID_CREDENTIALS() {
        given(adminRepository.findByLoginId("nobody")).willReturn(Optional.empty());

        assertCode(() -> service.verify("nobody", "rawPw"), AuthErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void verify_비밀번호_불일치는_INVALID_CREDENTIALS() {
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(admin(null, AdminStatus.ACTIVE, null)));
        given(passwordEncoder.matches("wrong", "hashedPw")).willReturn(false);

        assertCode(() -> service.verify("admin01", "wrong"), AuthErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void verify_삭제된_계정은_INVALID_CREDENTIALS() {
        given(adminRepository.findByLoginId("admin01"))
                .willReturn(Optional.of(admin(null, AdminStatus.ACTIVE, "2026-01-01 00:00:00")));

        assertCode(() -> service.verify("admin01", "rawPw"), AuthErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void verify_승인대기_계정은_ACCOUNT_PENDING() {
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(admin(7L, AdminStatus.PENDING, null)));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);

        assertCode(() -> service.verify("admin01", "rawPw"), AuthErrorCode.ACCOUNT_PENDING);
    }

    @Test
    void verify_정지_계정은_ACCOUNT_INACTIVE() {
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(admin(7L, AdminStatus.INACTIVE, null)));
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);

        assertCode(() -> service.verify("admin01", "rawPw"), AuthErrorCode.ACCOUNT_INACTIVE);
    }

    @Test
    void getActiveAccount_활성_계정은_정상_반환() {
        given(adminRepository.findById(1L)).willReturn(Optional.of(admin(7L, AdminStatus.ACTIVE, null)));
        given(companyRepository.findById(7L)).willReturn(Optional.of(company()));

        VerifiedAccount result = service.getActiveAccount(1L);

        assertThat(result.adminId()).isEqualTo(1L);
        assertThat(result.companyName()).isEqualTo("테스트업체");
    }

    @Test
    void getActiveAccount_없는_계정은_INVALID_CREDENTIALS() {
        given(adminRepository.findById(9L)).willReturn(Optional.empty());

        assertCode(() -> service.getActiveAccount(9L), AuthErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void getActiveAccount_삭제된_계정은_INVALID_CREDENTIALS() {
        given(adminRepository.findById(1L))
                .willReturn(Optional.of(admin(null, AdminStatus.ACTIVE, "2026-01-01 00:00:00")));

        assertCode(() -> service.getActiveAccount(1L), AuthErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void getAccountSummaries_삭제된_계정은_제외하고_회사명을_한번에_조회한다() {
        Admin a1 = Admin.of(1L, "admin01", "pw", "관리자", "010", null, 7L, AdminStatus.ACTIVE, null, null);
        Admin a2 = Admin.of(2L, "admin02", "pw", "직원", "010", null, 7L, AdminStatus.PENDING, null, null);
        Admin deleted = Admin.of(3L, "admin03", "pw", "삭제됨", "010", null, 7L, AdminStatus.ACTIVE, null, "2026-01-01 00:00:00");
        Admin noCompany = Admin.of(4L, "admin04", "pw", "무소속", "010", null, null, AdminStatus.ACTIVE, null, null);
        given(adminRepository.findAllByIdIn(List.of(1L, 2L, 3L, 4L))).willReturn(List.of(a1, a2, deleted, noCompany));
        given(companyRepository.findAllByIdIn(List.of(7L))).willReturn(List.of(company()));

        List<AccountSummary> result = service.getAccountSummaries(List.of(1L, 2L, 3L, 4L));

        assertThat(result).extracting(AccountSummary::adminId).containsExactly(1L, 2L, 4L);
        assertThat(result.get(0).companyName()).isEqualTo("테스트업체");
        assertThat(result.get(1).loginId()).isEqualTo("admin02");
        assertThat(result.get(2).companyId()).isNull();
        assertThat(result.get(2).companyName()).isNull();
    }

    @Test
    void getAccountSummaries_빈_목록이면_조회하지_않는다() {
        assertThat(service.getAccountSummaries(List.of())).isEmpty();
        assertThat(service.getAccountSummaries(null)).isEmpty();

        verifyNoInteractions(adminRepository, companyRepository);
    }

    @Test
    void getAccountSummaries_회사가_없는_계정만이면_회사를_조회하지_않는다() {
        Admin noCompany = Admin.of(4L, "admin04", "pw", "무소속", "010", null, null, AdminStatus.ACTIVE, null, null);
        given(adminRepository.findAllByIdIn(List.of(4L))).willReturn(List.of(noCompany));

        List<AccountSummary> result = service.getAccountSummaries(List.of(4L));

        assertThat(result).hasSize(1);
        verifyNoInteractions(companyRepository);
    }

    @Test
    void getActiveAccount_정지_계정은_ACCOUNT_INACTIVE() {
        given(adminRepository.findById(1L)).willReturn(Optional.of(admin(7L, AdminStatus.INACTIVE, null)));

        assertCode(() -> service.getActiveAccount(1L), AuthErrorCode.ACCOUNT_INACTIVE);
    }

    @Test
    void verify_비밀번호_설정_대기_계정은_비밀번호_비교_없이_PASSWORD_NOT_SET() {
        Admin pending = admin(7L, AdminStatus.ACTIVE, null);
        pending.restorePasswordSetup(java.time.LocalDateTime.now().plusDays(1), 0);
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(pending));

        assertCode(() -> service.verify("admin01", "anything"), AuthErrorCode.PASSWORD_NOT_SET);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void verify_비밀번호_설정_만료_계정도_PASSWORD_NOT_SET() {
        Admin expired = admin(7L, AdminStatus.ACTIVE, null);
        expired.restorePasswordSetup(java.time.LocalDateTime.now().minusDays(1), 0);
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(expired));

        assertCode(() -> service.verify("admin01", "anything"), AuthErrorCode.PASSWORD_NOT_SET);
    }

    @Test
    void getAccountSummaries_설정_대기_여부를_반영한다() {
        Admin pending = admin(7L, AdminStatus.ACTIVE, null);
        pending.restorePasswordSetup(java.time.LocalDateTime.now().plusDays(1), 0);
        given(adminRepository.findAllByIdIn(List.of(1L))).willReturn(List.of(pending));
        given(companyRepository.findAllByIdIn(List.of(7L))).willReturn(List.of(company()));

        assertThat(service.getAccountSummaries(List.of(1L)).get(0).passwordSetupPending()).isTrue();
    }
}
