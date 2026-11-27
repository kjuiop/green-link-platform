package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.ShopManagedAccountCommand;
import com.greenlink.core.application.auth.dto.VerifiedAccount;
import com.greenlink.core.application.auth.usecase.ShopManagedAccountUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

@Service
@Transactional(readOnly = true)
public class ShopManagedAccountService implements ShopManagedAccountUseCase {

    static final long SETUP_VALID_DAYS = 7;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AdminRepository adminRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public ShopManagedAccountService(AdminRepository adminRepository,
                                     CompanyRepository companyRepository,
                                     PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public VerifiedAccount create(ShopManagedAccountCommand cmd) {
        if (cmd.role() != AdminRole.PARTNER_ADMIN && cmd.role() != AdminRole.BRAND_ADMIN) {
            throw new BusinessException(AuthErrorCode.SHOP_SIGNUP_ROLE_INVALID);
        }
        Company company = companyRepository.findById(cmd.companyId())
            .orElseThrow(() -> new BusinessException(AuthErrorCode.SHOP_ACCESS_COMPANY_NOT_FOUND));
        if (adminRepository.existsByLoginId(cmd.loginId())) {
            throw new BusinessException(AuthErrorCode.SHOP_LOGIN_ID_DUPLICATED);
        }
        Admin admin;
        try {
            admin = adminRepository.save(Admin.createShopManaged(
                cmd.loginId(), unusablePasswordHash(), cmd.name(), cmd.phone(), cmd.email(),
                company.getId(), newExpiry(), cmd.role()));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(AuthErrorCode.SHOP_LOGIN_ID_DUPLICATED);
        }
        return new VerifiedAccount(
            admin.getId(), admin.getLoginId(), admin.getName(),
            company.getId(), company.getCompanyName(),
            company.isOnurimEnabled(), company.isSiteEnabled(),
            admin.isOnurimEnabled(), admin.getRole().name());
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void completePasswordSetup(String loginId, String phone, String newPassword) {
        Admin admin = adminRepository.findByLoginIdForUpdate(loginId)
            .filter(a -> !a.isDeleted())
            .orElseThrow(this::setupFailed);
        if (!admin.canSetupPassword(LocalDateTime.now(KST))) {
            throw setupFailed();
        }
        if (!samePhone(admin.getPhone(), phone)) {
            admin.recordPasswordSetupFailure();
            adminRepository.save(admin);
            throw setupFailed();
        }
        admin.completePasswordSetup(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }

    @Override
    @Transactional
    public void reissuePasswordSetup(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
            .filter(a -> !a.isDeleted())
            .orElseThrow(() -> new BusinessException(AuthErrorCode.SHOP_ACCESS_ADMIN_NOT_FOUND));
        if (!admin.isPasswordSetupPending()) {
            throw new BusinessException(AuthErrorCode.PASSWORD_SETUP_NOT_PENDING);
        }
        admin.reissuePasswordSetup(newExpiry());
        adminRepository.save(admin);
    }

    private BusinessException setupFailed() {
        return new BusinessException(AuthErrorCode.PASSWORD_SETUP_FAILED);
    }

    private LocalDateTime newExpiry() {
        return LocalDateTime.now(KST).plusDays(SETUP_VALID_DAYS);
    }

    private String unusablePasswordHash() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return passwordEncoder.encode(Base64.getEncoder().encodeToString(bytes));
    }

    private static boolean samePhone(String stored, String provided) {
        String a = digits(stored);
        String b = digits(provided);
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private static String digits(String raw) {
        return raw == null ? "" : raw.replaceAll("[^0-9]", "");
    }
}
