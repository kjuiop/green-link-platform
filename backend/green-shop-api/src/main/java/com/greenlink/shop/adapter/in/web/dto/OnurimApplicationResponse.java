package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationStatus;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationView;
import com.greenlink.shopcore.application.onurim.dto.OnurimCompanyType;

import java.time.LocalDateTime;

public record OnurimApplicationResponse(
    Long id,
    OnurimApplicationStatus status,
    OnurimCompanyType desiredType,
    OnurimCompanyType approvedType,
    String rejectReason,
    LocalDateTime processedAt,
    LocalDateTime createdAt
) {
    public static OnurimApplicationResponse from(OnurimApplicationView v) {
        return new OnurimApplicationResponse(v.id(), v.status(), v.desiredType(), v.approvedType(),
            v.rejectReason(), v.processedAt(), v.createdAt());
    }
}
