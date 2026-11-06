package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.vendor.dto.CreateVendorCommand;
import jakarta.validation.constraints.NotBlank;

public record CreateVendorRequest(
    @NotBlank String companyName,
    String businessNo,
    String faxNumber,
    String companyPhone,
    String address,
    String detailAddress,
    @NotBlank String managerName,
    @NotBlank String phone,
    String email,
    @NotBlank String loginId,
    @NotBlank String password,
    String companyType
) {
    public CreateVendorCommand toCommand() {
        return new CreateVendorCommand(
            companyName,
            businessNo,
            faxNumber,
            companyPhone,
            address,
            detailAddress,
            managerName,
            phone,
            email,
            loginId,
            password,
            companyType
        );
    }
}
