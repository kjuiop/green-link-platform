package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShopPasswordSetupRequest(
    @NotBlank @Size(max = 30) String loginId,
    @NotBlank @Size(max = 20) String phone,
    @NotBlank @Size(min = 8, max = 64) @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$") String newPassword
) {
    @Override
    public String toString() {
        return "ShopPasswordSetupRequest[loginId=" + loginId + ", newPassword=****]";
    }
}
