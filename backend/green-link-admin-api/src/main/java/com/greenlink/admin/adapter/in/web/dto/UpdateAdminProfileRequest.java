package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.admin.dto.UpdateAdminProfileCommand;
import jakarta.validation.constraints.NotBlank;

public record UpdateAdminProfileRequest(
    @NotBlank String name,
    @NotBlank String phone
) {
    public UpdateAdminProfileCommand toCommand(Long adminId) {
        return new UpdateAdminProfileCommand(adminId, name, phone);
    }
}
