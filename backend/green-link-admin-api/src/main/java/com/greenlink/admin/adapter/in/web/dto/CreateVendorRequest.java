package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.vendor.dto.CreateVendorCommand;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;

public record CreateVendorRequest(
    @NotBlank String companyName,
    String businessNo,
    String address,
    @NotBlank String managerName,
    @NotBlank String phone,
    @NotBlank String loginId,
    @NotBlank String password
) {
    public CreateVendorCommand toCommand(PasswordEncoder passwordEncoder) {
        return new CreateVendorCommand(
            companyName,
            businessNo,
            address,
            managerName,
            phone,
            loginId,
            passwordEncoder.encode(password)
        );
    }
}
