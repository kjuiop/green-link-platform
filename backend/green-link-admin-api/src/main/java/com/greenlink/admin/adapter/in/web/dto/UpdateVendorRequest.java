package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.application.vendor.dto.UpdateVendorCommand;
import com.greenlink.core.application.vendor.dto.VendorSignupCommand.RegionInput;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateVendorRequest(
    @NotBlank String companyName,
    String businessNo,
    String faxNumber,
    String companyPhone,
    String address,
    String detailAddress,
    @NotBlank String managerName,
    @NotBlank String phone,
    String email,
    String companyType,
    List<RegionInput> regions,
    List<String> capabilities,
    String bankName,
    String accountNumber,
    String accountHolder,
    String businessStatus
) {
    public UpdateVendorCommand toCommand() {
        return new UpdateVendorCommand(
            companyName, businessNo, faxNumber, companyPhone, address, detailAddress,
            managerName, phone, email, companyType, regions, capabilities,
            bankName, accountNumber, accountHolder, businessStatus
        );
    }
}
