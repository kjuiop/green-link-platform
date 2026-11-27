package com.greenlink.core.application.auth.dto;

public record VendorLoginResult(String accessToken, Long vendorId, String role, String companyType) {}
