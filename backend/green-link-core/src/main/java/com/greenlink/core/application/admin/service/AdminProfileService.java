package com.greenlink.core.application.admin.service;

import com.greenlink.core.application.admin.dto.AdminProfileResult;
import com.greenlink.core.application.admin.dto.UpdateAdminProfileCommand;
import com.greenlink.core.application.admin.usecase.GetAdminProfileUseCase;
import com.greenlink.core.application.admin.usecase.UpdateAdminProfileUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.admin.AdminErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminProfileService implements GetAdminProfileUseCase, UpdateAdminProfileUseCase {

    private final AdminRepository adminRepository;

    public AdminProfileService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public AdminProfileResult getProfile(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new BusinessException(AdminErrorCode.ADMIN_NOT_FOUND));
        return new AdminProfileResult(admin.getId(), admin.getLoginId(), admin.getName(), admin.getPhone());
    }

    @Override
    @Transactional
    public void updateProfile(UpdateAdminProfileCommand cmd) {
        Admin admin = adminRepository.findById(cmd.adminId())
            .orElseThrow(() -> new BusinessException(AdminErrorCode.ADMIN_NOT_FOUND));
        admin.updateProfile(cmd.name(), cmd.phone());
        adminRepository.save(admin);
    }
}
