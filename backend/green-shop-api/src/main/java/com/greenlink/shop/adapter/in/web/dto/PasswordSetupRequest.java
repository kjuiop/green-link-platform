package com.greenlink.shop.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordSetupRequest(
    @NotBlank @Size(max = 30) String loginId,
    @NotBlank @Size(max = 20) @Pattern(regexp = "[0-9-]+") String phone,
    @NotBlank @Size(min = 8, max = 64) @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$") String newPassword
) {
    @Override
    public String toString() {
        return "PasswordSetupRequest[loginId=" + loginId + ", newPassword=****]";
    }
}
