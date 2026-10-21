package com.greenlink.core.application.vendor.dto;

public record VendorResult(
    Long id,
    String companyName,
    String businessNo,
    String managerName,
    String phone,
    String address,
    String loginId,
    String status,
    String createdAt
) {}
