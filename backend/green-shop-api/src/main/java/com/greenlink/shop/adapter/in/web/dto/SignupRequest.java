package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.auth.dto.SignupCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank @Size(min = 4, max = 30) @Pattern(regexp = "[A-Za-z0-9_.-]+") String loginId,
    @NotBlank @Size(min = 8, max = 64) @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$") String password,
    @NotBlank @Size(max = 50) String name,
    @NotBlank @Size(min = 9, max = 20) @Pattern(regexp = "[0-9-]+") String phone,
    @Size(max = 100) String email,
    @NotBlank @Size(max = 20) String businessNo,
    @Size(max = 100) String companyName,
    @Size(max = 50) String ceoName,
    @Size(max = 30) String requestedRole
) {
    public SignupCommand toCommand() {
        return new SignupCommand(loginId, password, name, phone, email, businessNo, companyName, ceoName, requestedRole);
    }
}
