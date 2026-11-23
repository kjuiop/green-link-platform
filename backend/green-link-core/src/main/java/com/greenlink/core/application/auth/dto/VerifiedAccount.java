package com.greenlink.core.application.auth.dto;

public record VerifiedAccount(Long adminId, String loginId, String name, String companyName, boolean superAdmin) {}
