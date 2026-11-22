package com.greenlink.core.application.fax.dto;

import com.greenlink.core.domain.fax.model.FaxMessage;

import java.time.LocalDateTime;

public record FaxMessageResult(
        Long id,
        Long orderId,
        String orderNumber,
        int sequence,
        Long vendorCompanyId,
        String vendorCompanyName,
        String toNumber,
        String status,
        String failReason,
        boolean manual,
        LocalDateTime requestedAt,
        LocalDateTime completedAt
) {
    public static FaxMessageResult of(FaxMessage m, String orderNumber, String vendorCompanyName) {
        return new FaxMessageResult(m.getId(), m.getOrderId(), orderNumber, m.getSequence(), m.getVendorCompanyId(),
                vendorCompanyName, m.getToNumber(), m.getStatus().name(), m.getFailReason(), m.isManual(),
                m.getRequestedAt(), m.getCompletedAt());
    }
}
