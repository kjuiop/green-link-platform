package com.greenlink.core.application.admin.usecase;

import com.greenlink.core.application.admin.dto.AdminProfileResult;

public interface GetAdminProfileUseCase {
    AdminProfileResult getProfile(Long adminId);
}
