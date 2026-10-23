package com.greenlink.core.application.auth.dto;

public record AdminLoginResult(String accessToken, String name, Long vendorId, String companyName) {}
