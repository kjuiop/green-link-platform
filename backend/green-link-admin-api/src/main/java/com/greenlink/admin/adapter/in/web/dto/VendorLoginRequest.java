package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.auth.dto.VendorLoginCommand;
import jakarta.validation.constraints.NotBlank;

public record VendorLoginRequest(
    @NotBlank String loginId,
    @NotBlank String password
) {
    public VendorLoginCommand toCommand() {
        return new VendorLoginCommand(loginId, password);
    }
}
