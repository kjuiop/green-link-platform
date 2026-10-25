package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.vendor.dto.CreateVendorCommand;
import jakarta.validation.constraints.NotBlank;

public record CreateVendorRequest(
    @NotBlank String companyName,
    String businessNo,
    String address,
    String detailAddress,
    @NotBlank String managerName,
    @NotBlank String phone,
    @NotBlank String loginId,
    @NotBlank String password,
    String companyType
) {
    public CreateVendorCommand toCommand() {
        return new CreateVendorCommand(
            companyName,
            businessNo,
            address,
            detailAddress,
            managerName,
            phone,
            loginId,
            password,
            companyType
        );
    }
}
