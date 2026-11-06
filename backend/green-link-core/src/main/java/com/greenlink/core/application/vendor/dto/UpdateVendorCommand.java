package com.greenlink.core.application.vendor.dto;

import com.greenlink.core.application.vendor.dto.VendorSignupCommand.RegionInput;

import java.util.List;

public record UpdateVendorCommand(
    String companyName,
    String businessNo,
    String faxNumber,
    String companyPhone,
    String address,
    String detailAddress,
    String managerName,
    String phone,
    String email,
    String companyType,
    List<RegionInput> regions,
    List<String> capabilities
) {}
