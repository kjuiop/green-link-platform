package com.greenlink.core.application.auth.dto;

public record VerifiedAccount(
    Long adminId,
    String loginId,
    String name,
    Long companyId,
    String companyName,
    boolean companyOnurimEnabled,
    boolean companySiteEnabled,
    boolean adminOnurimEnabled,
    String role
) {}
