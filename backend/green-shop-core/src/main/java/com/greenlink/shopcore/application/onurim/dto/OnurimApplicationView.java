package com.greenlink.shopcore.application.onurim.dto;

import java.time.LocalDateTime;

public record OnurimApplicationView(
    Long id,
    OnurimApplicationStatus status,
    OnurimCompanyType desiredType,
    OnurimCompanyType approvedType,
    String rejectReason,
    LocalDateTime processedAt,
    LocalDateTime createdAt
) {}
