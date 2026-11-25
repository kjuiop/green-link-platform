package com.greenlink.shopcore.application.auth.dto;

public record VerifiedIdentity(Long adminId, String loginId, String name, Long companyId, String companyName,
                               boolean companyOnurimEnabled, boolean companySiteEnabled,
                               boolean adminOnurimEnabled, boolean superAdmin) {}
