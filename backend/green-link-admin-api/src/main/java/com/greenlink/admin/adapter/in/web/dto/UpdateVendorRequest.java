package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.vendor.dto.UpdateVendorCommand;
import jakarta.validation.constraints.NotBlank;

public record UpdateVendorRequest(
    @NotBlank String companyName,
    String businessNo,
    String address,
    @NotBlank String managerName,
    @NotBlank String phone
) {
    public UpdateVendorCommand toCommand() {
        return new UpdateVendorCommand(companyName, businessNo, address, managerName, phone);
    }
}
