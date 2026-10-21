package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.VendorLoginCommand;
import com.greenlink.core.application.auth.dto.VendorLoginResult;
import com.greenlink.core.application.auth.port.TokenProvider;
import com.greenlink.core.application.auth.usecase.VendorLoginUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VendorLoginService implements VendorLoginUseCase {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public VendorLoginService(AdminRepository adminRepository,
                               PasswordEncoder passwordEncoder,
                               TokenProvider tokenProvider) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public VendorLoginResult login(VendorLoginCommand cmd) {
        Admin admin = adminRepository.findByLoginId(cmd.loginId())
            .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(cmd.password(), admin.getPassword())) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        if (!admin.isVendor()) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        if (admin.getStatus() == AdminStatus.PENDING) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_PENDING);
        }

        if (admin.getStatus() == AdminStatus.INACTIVE) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_INACTIVE);
        }

        String token = tokenProvider.generateToken(admin.getId(), List.of("VENDOR"));
        return new VendorLoginResult(token, admin.getCompanyId());
    }
}
