package com.greenlink.core.application.settlement.dto;

public record SettlementOrderResult(
        Long orderId,
        String orderNumber,
        String counterpartCompanyName,
        String productName,
        String placeName,
        String ordererName,
        String deliveredAt,
        long orderAmount,
        Long vendorAmount,
        Long extraCost
) {
    /** 발주금액 = 주문금액 + 추가비용 */
    public long totalAmount() {
        return orderAmount + (extraCost != null ? extraCost : 0L);
    }
}
