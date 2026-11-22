package com.greenlink.core.application.point.dto;

public record CompanyPointResult(
        Long companyId,
        String companyName,
        String companyType,
        long balance,
        long depositBalance
) {}
