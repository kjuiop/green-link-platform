package com.greenlink.core.application.vendor.dto;

public record CreateVendorCommand(
    String companyName,
    String businessNo,
    String address,
    String managerName,
    String phone,
    String loginId,
    String encodedPassword
) {}
