package com.greenlink.core.application.auth.dto;

public record AccountSummary(
    Long adminId,
    String loginId,
    String name,
    Long companyId,
    String companyName
) {}
