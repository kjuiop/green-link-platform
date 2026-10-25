package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.vendor.dto.VendorSignupCommand;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record VendorSignupRequest(
    @NotBlank String loginId,
    @NotBlank String password,
    @NotBlank String name,
    @NotBlank String phone,
    String businessNo,
    @NotBlank String companyName,
    String ceoName,
    String companyPhone,
    String address,
    String detailAddress,
    List<String> roles
) {
    public VendorSignupCommand toCommand() {
        return new VendorSignupCommand(
            loginId,
            password,
            name,
            phone,
            businessNo,
            companyName,
            ceoName,
            companyPhone,
            address,
            detailAddress,
            roles
        );
    }
}
