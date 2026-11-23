package com.greenlink.core.application.order.dto;

public record PendingAdjustment(
        Long id,
        String requesterName,
        boolean requestedByProxy,
        String requestedAt,
        long beforeTotal,
        long adjustAmount,
        long afterTotal,
        String reason
) {

    public PendingAdjustment toVendorView(Long vendorFee) {
        long fee = vendorFee != null ? vendorFee : 0L;
        return new PendingAdjustment(id, requesterName, requestedByProxy, requestedAt,
                beforeTotal - fee, adjustAmount, afterTotal - fee, reason);
    }
}
