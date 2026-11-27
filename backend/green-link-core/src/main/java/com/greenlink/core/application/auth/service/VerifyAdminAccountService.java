package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.AccountSummary;
import com.greenlink.core.application.auth.dto.VerifiedAccount;
import com.greenlink.core.application.auth.usecase.VerifyAdminAccountUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VerifyAdminAccountService implements VerifyAdminAccountUseCase {

    private final AdminRepository adminRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public VerifyAdminAccountService(AdminRepository adminRepository,
                                     CompanyRepository companyRepository,
                                     PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public VerifiedAccount verify(String loginId, String rawPassword) {
        Admin admin = adminRepository.findByLoginId(loginId)
            .filter(a -> !a.isDeleted())
            .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_CREDENTIALS));
        if (admin.isPasswordSetupPending()) {
            throw new BusinessException(AuthErrorCode.PASSWORD_NOT_SET);
        }
        if (!passwordEncoder.matches(rawPassword, admin.getPassword())) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        return toVerified(admin);
    }

    @Override
    public VerifiedAccount getActiveAccount(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
            .filter(a -> !a.isDeleted())
            .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_CREDENTIALS));
        return toVerified(admin);
    }

    @Override
    public List<AccountSummary> getAccountSummaries(Collection<Long> adminIds) {
        if (adminIds == null || adminIds.isEmpty()) {
            return List.of();
        }
        List<Admin> admins = adminRepository.findAllByIdIn(adminIds).stream()
            .filter(a -> !a.isDeleted())
            .toList();
        List<Long> companyIds = admins.stream()
            .map(Admin::getCompanyId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, String> companyNames = companyIds.isEmpty()
            ? Map.of()
            : companyRepository.findAllByIdIn(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, Company::getCompanyName, (a, b) -> a));
        return admins.stream()
            .map(a -> new AccountSummary(
                a.getId(), a.getLoginId(), a.getName(),
                a.getCompanyId(),
                a.getCompanyId() == null ? null : companyNames.get(a.getCompanyId()),
                a.isPasswordSetupPending()))
            .toList();
    }

    private VerifiedAccount toVerified(Admin admin) {
        if (admin.getStatus() == AdminStatus.PENDING) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_PENDING);
        }
        if (admin.getStatus() == AdminStatus.INACTIVE) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_INACTIVE);
        }
        Long companyId = admin.getCompanyId();
        Company company = companyId == null ? null : companyRepository.findById(companyId).orElse(null);
        return new VerifiedAccount(
            admin.getId(), admin.getLoginId(), admin.getName(),
            companyId,
            company == null ? null : company.getCompanyName(),
            company != null && company.isOnurimEnabled(),
            company != null && company.isSiteEnabled(),
            admin.isOnurimEnabled(),
            admin.getRole().name());
    }
}
