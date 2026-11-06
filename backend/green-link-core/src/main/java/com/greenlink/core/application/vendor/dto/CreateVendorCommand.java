package com.greenlink.core.application.vendor.dto;

public record CreateVendorCommand(
    String companyName,
    String businessNo,
    String faxNumber,
    String companyPhone,
    String address,
    String detailAddress,
    String managerName,
    String phone,
    String email,
    String loginId,
    String password,
    String companyType
) {}
