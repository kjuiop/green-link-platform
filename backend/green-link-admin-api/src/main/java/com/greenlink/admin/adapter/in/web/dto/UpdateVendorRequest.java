package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.vendor.dto.UpdateVendorCommand;
import jakarta.validation.constraints.NotBlank;

public record UpdateVendorRequest(
    @NotBlank String companyName,
    String businessNo,
    String faxNumber,
    String address,
    String detailAddress,
    @NotBlank String managerName,
    @NotBlank String phone,
    String companyType
) {
    public UpdateVendorCommand toCommand() {
        return new UpdateVendorCommand(companyName, businessNo, faxNumber, address, detailAddress, managerName, phone, companyType);
    }
}
