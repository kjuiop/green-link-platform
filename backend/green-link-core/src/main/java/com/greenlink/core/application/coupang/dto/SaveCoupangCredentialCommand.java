package com.greenlink.core.application.coupang.dto;

import java.time.LocalDate;

public record SaveCoupangCredentialCommand(
        Long companyId,
        String vendorCode,
        String accessKey,
        String secretKey,
        LocalDate keyIssuedAt,
        boolean enabled
) {}
