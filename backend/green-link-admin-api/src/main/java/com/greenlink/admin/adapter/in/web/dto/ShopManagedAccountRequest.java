package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.auth.dto.ShopManagedAccountCommand;
import com.greenlink.core.domain.admin.model.AdminRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShopManagedAccountRequest(
    @NotBlank @Size(min = 4, max = 30) @Pattern(regexp = "[A-Za-z0-9_.-]+") String loginId,
    @NotBlank @Size(max = 50) String name,
    @NotBlank @Size(min = 9, max = 20) @Pattern(regexp = "[0-9-]+") String phone,
    @Size(max = 100) String email,
    @NotNull Long companyId,
    @NotNull AdminRole role
) {
    public ShopManagedAccountCommand toCommand() {
        return new ShopManagedAccountCommand(loginId, name, phone,
            email == null || email.isBlank() ? null : email, companyId, role);
    }
}
