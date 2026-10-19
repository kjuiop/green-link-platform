package com.greenlink.core.application.admin.usecase;

import com.greenlink.core.application.admin.dto.UpdateAdminProfileCommand;

public interface UpdateAdminProfileUseCase {
    void updateProfile(UpdateAdminProfileCommand cmd);
}
