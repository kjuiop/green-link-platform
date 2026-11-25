package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.auth.dto.ShopSignupCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShopSignupRequest(
    @NotBlank @Size(min = 4, max = 30) @Pattern(regexp = "[A-Za-z0-9_.-]+") String loginId,
    @NotBlank @Size(min = 8, max = 64) @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$") String password,
    @NotBlank @Size(max = 50) String name,
    @NotBlank @Size(min = 9, max = 20) @Pattern(regexp = "[0-9-]+") String phone,
    @Size(max = 100) String email,
    @NotBlank String businessNo,
    @Size(max = 100) String companyName,
    @Size(max = 50) String ceoName,
    String requestedRole
) {
    public ShopSignupCommand toCommand() {
        return new ShopSignupCommand(loginId, password, name, phone, blankToNull(email),
            businessNo, companyName, blankToNull(ceoName), requestedRole);
    }

    @Override
    public String toString() {
        return "ShopSignupRequest[loginId=" + loginId + ", businessNo=" + businessNo + ", password=****]";
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v;
    }
}
