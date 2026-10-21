package com.greenlink.core.application.vendor.dto;

public record UpdateVendorCommand(
    String companyName,
    String businessNo,
    String address,
    String detailAddress,
    String managerName,
    String phone
) {}
