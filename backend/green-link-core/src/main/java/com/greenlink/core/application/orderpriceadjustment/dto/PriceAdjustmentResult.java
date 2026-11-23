package com.greenlink.core.application.orderpriceadjustment.dto;

public record PriceAdjustmentResult(
        Long id,
        Long orderId,
        String requesterName,
        boolean requestedByProxy,
        String requestedAt,
        long beforeTotal,
        long adjustAmount,
        long afterTotal,
        String reason,
        String status,
        String decidedByName,
        boolean decidedByProxy,
        String decidedAt,
        String rejectReason
) {

    /** 수주사 시점: 발주금액에서 수수료를 제외한 금액으로 치환한다. 조정금액은 전액 수주사 몫이라 그대로 둔다. */
    public PriceAdjustmentResult toVendorView(Long vendorFee) {
        long fee = vendorFee != null ? vendorFee : 0L;
        return new PriceAdjustmentResult(id, orderId, requesterName, requestedByProxy, requestedAt,
                beforeTotal - fee, adjustAmount, afterTotal - fee, reason, status,
                decidedByName, decidedByProxy, decidedAt, rejectReason);
    }
}
