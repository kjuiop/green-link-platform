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

        Long companyId = admin.getCompanyId();
        String role = "SUPER_ADMIN";
        String companyName = null;

        if (companyId != null) {
            Company company = companyRepository.findById(companyId).orElse(null);
            if (company != null) {
                companyName = company.getCompanyName();
                if (company.getType() == CompanyType.VENDOR) {
                    role = "VENDOR";
                } else if (company.getType() == CompanyType.ORDERING) {
                    role = "ORDERING";
                } else {
                    // BOTH 타입은 VENDOR로 처리
                    role = "VENDOR";
                }
            }
        }

        String token = tokenProvider.generateToken(admin.getId(), List.of(role), companyId);

        return new AdminLoginResult(token, admin.getName(), companyId, companyName);
    }
}
