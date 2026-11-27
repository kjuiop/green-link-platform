package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.ShopManagedAccountCommand;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShopManagedAccountServiceTest {

    @Mock AdminRepository adminRepository;
    @Mock CompanyRepository companyRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private ShopManagedAccountService service;

    @BeforeEach
    void setUp() {
        service = new ShopManagedAccountService(adminRepository, companyRepository, passwordEncoder);
    }

    private Company company() {
        return Company.of(7L, null, "파트너사", null, null, null, null, null, CompanyType.VENDOR, CompanyStatus.ACTIVE, false,
            null, null, null, null, null, null);
    }

    private ShopManagedAccountCommand cmd() {
        return new ShopManagedAccountCommand("brand01", "홍길동", "010-1234-5678", null, 7L);
    }

    private Admin pending(LocalDateTime expiresAt, int attempts) {
        Admin a = Admin.of(1L, "brand01", "randomHash", "홍길동", "010-1234-5678", null, 7L, AdminStatus.ACTIVE, null, null);
        a.restoreAccess(AdminRole.PARTNER_ADMIN, false);
        a.restorePasswordSetup(expiresAt, attempts);
        return a;
    }

    private void assertCode(Runnable call, AuthErrorCode expected) {
        assertThatThrownBy(call::run)
            .isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 계정을_만들면_비밀번호_없이_설정_대기_상태로_저장된다() {
        given(companyRepository.findById(7L)).willReturn(Optional.of(company()));
        given(adminRepository.existsByLoginId("brand01")).willReturn(false);
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> {
            Admin a = inv.getArgument(0);
            Admin saved = Admin.of(10L, a.getLoginId(), a.getPassword(), a.getName(), a.getPhone(), a.getEmail(),
                a.getCompanyId(), a.getStatus(), null, null);
            saved.restoreAccess(AdminRole.PARTNER_ADMIN, a.isOnurimEnabled());
            saved.restorePasswordSetup(a.getPasswordSetupExpiresAt(), 0);
            return saved;
        });

        VerifiedAccount result = service.create(cmd());

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(captor.capture());
        Admin saved = captor.getValue();
        assertThat(result.adminId()).isEqualTo(10L);
        assertThat(result.companyId()).isEqualTo(7L);
        assertThat(saved.isOnurimEnabled()).isFalse();
        assertThat(saved.isPasswordSetupPending()).isTrue();
        assertThat(saved.getPasswordSetupExpiresAt()).isAfter(LocalDateTime.now().plusDays(6));
        assertThat(saved.getPassword()).isNotBlank();
    }

    @Test
    void 무작위_해시는_어떤_입력으로도_로그인에_쓸_수_없다() {
        given(companyRepository.findById(7L)).willReturn(Optional.of(company()));
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> inv.getArgument(0));

        service.create(cmd());

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(captor.capture());
        String hash = captor.getValue().getPassword();
        assertThat(passwordEncoder.matches("", hash)).isFalse();
        assertThat(passwordEncoder.matches("brand01", hash)).isFalse();
        assertThat(passwordEncoder.matches("01012345678", hash)).isFalse();
    }

    @Test
    void 없는_회사에는_계정을_만들_수_없다() {
        given(companyRepository.findById(7L)).willReturn(Optional.empty());

        assertCode(() -> service.create(cmd()), AuthErrorCode.SHOP_ACCESS_COMPANY_NOT_FOUND);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void 아이디가_중복이면_거부한다() {
        given(companyRepository.findById(7L)).willReturn(Optional.of(company()));
        given(adminRepository.existsByLoginId("brand01")).willReturn(true);

        assertCode(() -> service.create(cmd()), AuthErrorCode.SHOP_LOGIN_ID_DUPLICATED);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void 동시_생성으로_유니크_위반이_나도_중복_에러로_변환한다() {
        given(companyRepository.findById(7L)).willReturn(Optional.of(company()));
        given(adminRepository.save(any(Admin.class))).willThrow(new DataIntegrityViolationException("dup"));

        assertCode(() -> service.create(cmd()), AuthErrorCode.SHOP_LOGIN_ID_DUPLICATED);
    }

    @Test
    void 아이디와_휴대폰이_맞으면_비밀번호가_설정되고_대기가_해제된다() {
        Admin admin = pending(LocalDateTime.now().plusDays(3), 2);
        given(adminRepository.findByLoginIdForUpdate("brand01")).willReturn(Optional.of(admin));

        service.completePasswordSetup("brand01", "01012345678", "Passw0rd!");

        verify(adminRepository).save(admin);
        assertThat(admin.isPasswordSetupPending()).isFalse();
        assertThat(passwordEncoder.matches("Passw0rd!", admin.getPassword())).isTrue();
    }

    @Test
    void 휴대폰이_다르면_실패횟수를_저장하고_단일_에러로_거부한다() {
        Admin admin = pending(LocalDateTime.now().plusDays(3), 0);
        given(adminRepository.findByLoginIdForUpdate("brand01")).willReturn(Optional.of(admin));

        assertCode(() -> service.completePasswordSetup("brand01", "010-9999-9999", "Passw0rd!"),
            AuthErrorCode.PASSWORD_SETUP_FAILED);

        assertThat(admin.getPasswordSetupAttempts()).isEqualTo(1);
        assertThat(admin.isPasswordSetupPending()).isTrue();
        verify(adminRepository).save(admin);
    }

    @Test
    void 실패가_5회_쌓이면_올바른_휴대폰이어도_거부한다() {
        Admin admin = pending(LocalDateTime.now().plusDays(3), 5);
        given(adminRepository.findByLoginIdForUpdate("brand01")).willReturn(Optional.of(admin));

        assertCode(() -> service.completePasswordSetup("brand01", "010-1234-5678", "Passw0rd!"),
            AuthErrorCode.PASSWORD_SETUP_FAILED);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void 기간이_만료되면_거부한다() {
        Admin admin = pending(LocalDateTime.now().minusMinutes(1), 0);
        given(adminRepository.findByLoginIdForUpdate("brand01")).willReturn(Optional.of(admin));

        assertCode(() -> service.completePasswordSetup("brand01", "010-1234-5678", "Passw0rd!"),
            AuthErrorCode.PASSWORD_SETUP_FAILED);
    }

    @Test
    void 이미_설정된_계정은_다시_설정할_수_없다() {
        Admin done = Admin.of(1L, "brand01", "hash", "홍길동", "010-1234-5678", null, 7L, AdminStatus.ACTIVE, null, null);
        given(adminRepository.findByLoginIdForUpdate("brand01")).willReturn(Optional.of(done));

        assertCode(() -> service.completePasswordSetup("brand01", "010-1234-5678", "Passw0rd!"),
            AuthErrorCode.PASSWORD_SETUP_FAILED);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void 없는_아이디도_같은_에러다() {
        given(adminRepository.findByLoginIdForUpdate("nobody")).willReturn(Optional.empty());

        assertCode(() -> service.completePasswordSetup("nobody", "010-1234-5678", "Passw0rd!"),
            AuthErrorCode.PASSWORD_SETUP_FAILED);
    }

    @Test
    void 삭제된_계정도_같은_에러다() {
        Admin deleted = Admin.of(1L, "brand01", "hash", "홍길동", "010-1234-5678", null, 7L, AdminStatus.INACTIVE, null, "2026-11-01");
        deleted.restorePasswordSetup(LocalDateTime.now().plusDays(1), 0);
        given(adminRepository.findByLoginIdForUpdate("brand01")).willReturn(Optional.of(deleted));

        assertCode(() -> service.completePasswordSetup("brand01", "010-1234-5678", "Passw0rd!"),
            AuthErrorCode.PASSWORD_SETUP_FAILED);
    }

    @Test
    void 재발급하면_만료가_연장되고_실패횟수가_초기화된다() {
        Admin locked = pending(LocalDateTime.now().minusDays(1), 5);
        given(adminRepository.findById(1L)).willReturn(Optional.of(locked));

        service.reissuePasswordSetup(1L);

        verify(adminRepository).save(locked);
        assertThat(locked.getPasswordSetupAttempts()).isZero();
        assertThat(locked.canSetupPassword(LocalDateTime.now())).isTrue();
    }

    @Test
    void 이미_설정된_계정은_재발급을_거부한다() {
        Admin done = Admin.of(1L, "brand01", "hash", "홍길동", "010-1234-5678", null, 7L, AdminStatus.ACTIVE, null, null);
        given(adminRepository.findById(1L)).willReturn(Optional.of(done));

        assertCode(() -> service.reissuePasswordSetup(1L), AuthErrorCode.PASSWORD_SETUP_NOT_PENDING);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void 없는_계정은_재발급할_수_없다() {
        given(adminRepository.findById(1L)).willReturn(Optional.empty());

        assertCode(() -> service.reissuePasswordSetup(1L), AuthErrorCode.SHOP_ACCESS_ADMIN_NOT_FOUND);
    }
}
