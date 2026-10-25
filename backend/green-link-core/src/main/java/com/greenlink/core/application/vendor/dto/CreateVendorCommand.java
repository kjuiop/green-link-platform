package com.greenlink.core.application.vendor.dto;

public record CreateVendorCommand(
    String companyName,
    String businessNo,
    String address,
    String detailAddress,
    String managerName,
    String phone,
    String loginId,
    String password,
    String companyType
) {}
