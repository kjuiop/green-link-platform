package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.AdminLoginCommand;
import com.greenlink.core.application.auth.dto.AdminLoginResult;
import com.greenlink.core.application.auth.port.TokenProvider;
import com.greenlink.core.application.auth.usecase.AdminLoginUseCase;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AdminLoginService implements AdminLoginUseCase {

    private final AdminRepository adminRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AdminLoginService(AdminRepository adminRepository,
                              CompanyRepository companyRepository,
                              PasswordEncoder passwordEncoder,
                              TokenProvider tokenProvider) {
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AdminLoginResult login(AdminLoginCommand cmd) {
        var admin = adminRepository.findByLoginId(cmd.loginId())
            .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(cmd.password(), admin.getPassword())) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        if (admin.getStatus() == AdminStatus.PENDING) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_PENDING);
        }
        if (admin.getStatus() == AdminStatus.INACTIVE) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_INACTIVE);
        }

        if (admin.isSuperAdmin()) {
            String token = tokenProvider.generateToken(admin.getId(), List.of("SUPER_ADMIN"), admin.getCompanyId());
            return new AdminLoginResult(token, admin.getName(), admin.getCompanyId(), null);
        }

        Company company = Optional.ofNullable(admin.getCompanyId())
            .flatMap(companyRepository::findById)
            .filter(Company::isOnurimCompany)
            .orElseThrow(() -> new BusinessException(AuthErrorCode.ONURIM_NOT_ENABLED));
        if (!admin.isOnurimEnabled()) {
            throw new BusinessException(AuthErrorCode.ONURIM_NOT_ENABLED);
        }

        // BOTH 타입은 VENDOR로 처리
        String role = company.getType() == CompanyType.ORDERING ? "ORDERING" : "VENDOR";
        String token = tokenProvider.generateToken(admin.getId(), List.of(role), company.getId());

        return new AdminLoginResult(token, admin.getName(), company.getId(), company.getCompanyName());
    }
}
