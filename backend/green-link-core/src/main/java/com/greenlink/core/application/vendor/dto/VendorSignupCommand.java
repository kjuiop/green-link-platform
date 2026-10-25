package com.greenlink.core.application.vendor.dto;

import java.util.List;

public record VendorSignupCommand(
    String loginId,
    String password,
    String managerName,
    String phone,
    String businessNo,
    String companyName,
    String ceoName,
    String companyPhone,
    String address,
    String detailAddress,
    List<String> roles
) {}
