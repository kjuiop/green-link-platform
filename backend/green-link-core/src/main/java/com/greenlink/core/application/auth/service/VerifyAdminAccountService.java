package com.greenlink.core.application.auth.service;

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

    private VerifiedAccount toVerified(Admin admin) {
        if (admin.getStatus() == AdminStatus.PENDING) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_PENDING);
        }
        if (admin.getStatus() == AdminStatus.INACTIVE) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_INACTIVE);
        }
        Long companyId = admin.getCompanyId();
        String companyName = companyId == null ? null
            : companyRepository.findById(companyId).map(Company::getCompanyName).orElse(null);
        return new VerifiedAccount(admin.getId(), admin.getLoginId(), admin.getName(), companyName, companyId == null);
    }
}
