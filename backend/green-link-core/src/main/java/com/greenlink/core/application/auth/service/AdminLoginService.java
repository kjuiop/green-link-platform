package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.AdminLoginCommand;
import com.greenlink.core.application.auth.dto.AdminLoginResult;
import com.greenlink.core.application.auth.port.TokenProvider;
import com.greenlink.core.application.auth.usecase.AdminLoginUseCase;
import com.greenlink.core.application.menu.dto.MenuResult;
import com.greenlink.core.application.menu.usecase.GetMenusByRoleUseCase;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.model.Role;
import com.greenlink.core.domain.admin.port.AdminRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final GetMenusByRoleUseCase getMenusByRoleUseCase;

    public AdminLoginService(AdminRepository adminRepository,
                              PasswordEncoder passwordEncoder,
                              TokenProvider tokenProvider,
                              GetMenusByRoleUseCase getMenusByRoleUseCase) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.getMenusByRoleUseCase = getMenusByRoleUseCase;
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

        Role role = admin.isVendor() ? Role.VENDOR : Role.SUPER_ADMIN;
        String token = tokenProvider.generateToken(admin.getId(), List.of(role.name()));
        List<MenuResult> menus = getMenusByRoleUseCase.getMenusByRole(role);
        return new AdminLoginResult(token, admin.getName(), menus);
    }
}
