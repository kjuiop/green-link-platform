package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.auth.dto.AdminLoginCommand;
import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
    @NotBlank String loginId,
    @NotBlank String password
) {
    public AdminLoginCommand toCommand() {
        return new AdminLoginCommand(loginId, password);
    }
}
