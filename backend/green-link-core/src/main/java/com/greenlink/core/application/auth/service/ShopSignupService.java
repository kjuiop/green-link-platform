package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.ShopSignupCommand;
import com.greenlink.core.application.auth.dto.ShopSignupResult;
import com.greenlink.core.application.auth.dto.VerifiedAccount;
import com.greenlink.core.application.auth.usecase.ShopSignupUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ShopSignupService implements ShopSignupUseCase {

    private static final Set<String> JOINABLE_ROLES = Set.of("PARTNER_ADMIN", "BRAND_ADMIN");

    private final AdminRepository adminRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public ShopSignupService(AdminRepository adminRepository,
                             CompanyRepository companyRepository,
                             PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public ShopSignupResult signup(ShopSignupCommand cmd) {
        String businessNo = normalizeBusinessNo(cmd.businessNo());
        if (adminRepository.existsByLoginId(cmd.loginId())) {
            throw new BusinessException(AuthErrorCode.SHOP_LOGIN_ID_DUPLICATED);
        }
        String encoded = passwordEncoder.encode(cmd.password());

        try {
            Optional<Company> existing = companyRepository.findByBusinessNo(businessNo);
            if (existing.isPresent()) {
                return join(cmd, encoded, existing.get());
            }
            return found(cmd, encoded, businessNo);
        } catch (DataIntegrityViolationException e) {
            if (adminRepository.existsByLoginId(cmd.loginId())) {
                throw new BusinessException(AuthErrorCode.SHOP_LOGIN_ID_DUPLICATED);
            }
            throw new BusinessException(AuthErrorCode.SHOP_BUSINESS_NO_REGISTERED);
        }
    }

    @Override
    public boolean existsBusinessNo(String businessNo) {
        return companyRepository.existsByBusinessNo(normalizeBusinessNo(businessNo));
    }

    private ShopSignupResult found(ShopSignupCommand cmd, String encodedPassword, String businessNo) {
        if (cmd.companyName() == null || cmd.companyName().isBlank()) {
            throw new BusinessException(AuthErrorCode.SHOP_COMPANY_NAME_REQUIRED);
        }
        Company company = companyRepository.save(Company.createForShop(
            businessNo, cmd.companyName(), cmd.ceoName() != null ? cmd.ceoName() : "", cmd.phone()));
        Admin admin = adminRepository.save(Admin.createShopFounder(
            cmd.loginId(), encodedPassword, cmd.name(), cmd.phone(), cmd.email(), company.getId()));
        return new ShopSignupResult(toAccount(admin, company), company.getId(), true);
    }

    private ShopSignupResult join(ShopSignupCommand cmd, String encodedPassword, Company target) {
        if (cmd.requestedRole() == null) {
            throw new BusinessException(AuthErrorCode.SHOP_BUSINESS_NO_REGISTERED);
        }
        if (!JOINABLE_ROLES.contains(cmd.requestedRole())) {
            throw new BusinessException(AuthErrorCode.SHOP_SIGNUP_ROLE_INVALID);
        }
        Admin admin = adminRepository.save(Admin.createShopApplicant(
            cmd.loginId(), encodedPassword, cmd.name(), cmd.phone(), cmd.email()));
        return new ShopSignupResult(toAccount(admin, null), target.getId(), false);
    }

    private VerifiedAccount toAccount(Admin admin, Company company) {
        return new VerifiedAccount(
            admin.getId(), admin.getLoginId(), admin.getName(),
            company == null ? null : company.getId(),
            company == null ? null : company.getCompanyName(),
            company != null && company.isOnurimEnabled(),
            company != null && company.isSiteEnabled(),
            admin.isOnurimEnabled(),
            admin.isSuperAdmin());
    }

    private String normalizeBusinessNo(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("[^0-9]", "");
        if (digits.length() != 10) {
            throw new BusinessException(AuthErrorCode.INVALID_BUSINESS_NO);
        }
        return digits.substring(0, 3) + "-" + digits.substring(3, 5) + "-" + digits.substring(5);
    }
}
