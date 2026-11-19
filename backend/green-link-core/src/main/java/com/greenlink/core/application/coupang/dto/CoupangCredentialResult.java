package com.greenlink.core.application.coupang.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CoupangCredentialResult(
        Long companyId,
        String vendorCode,
        String accessKeyMasked,
        boolean secretKeyRegistered,
        LocalDate keyIssuedAt,
        LocalDate keyExpiresAt,
        Long daysUntilExpire,
        boolean expired,
        boolean enabled,
        LocalDateTime lastCollectedAt,
        String lastError,
        LocalDateTime lastErrorAt
) {}
