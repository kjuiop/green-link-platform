package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.admin.dto.CreateMemberCommand;
import com.greenlink.shopcore.domain.auth.ShopRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateMemberRequest(
    @NotBlank @Size(min = 4, max = 30) @Pattern(regexp = "[A-Za-z0-9_.-]+") String loginId,
    @NotBlank @Size(max = 50) String name,
    @NotBlank @Size(min = 9, max = 20) @Pattern(regexp = "[0-9-]+") String phone,
    @Size(max = 100) String email,
    @NotNull ShopRole role,
    Long companyId,
    Set<Long> brandIds
) {
    public CreateMemberCommand toCommand() {
        return new CreateMemberCommand(loginId, name, phone, email == null || email.isBlank() ? null : email,
            role, companyId, brandIds);
    }
}
