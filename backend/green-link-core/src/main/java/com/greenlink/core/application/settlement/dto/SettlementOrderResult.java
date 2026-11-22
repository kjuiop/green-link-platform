package com.greenlink.core.application.settlement.dto;

import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.settlement.port.SettlementQueryPort;

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
        Long extraCost,
        String orderingFeeDiscountType,
        long orderingDiscountAmount,
        long orderingSettlementAmount,
        Long vendorFee
) {
    public long totalAmount() {
        return orderAmount + (extraCost != null ? extraCost : 0L);
    }

    public static SettlementOrderResult forVendor(SettlementQueryPort.OrderSettlementRaw r, String counterpartName) {
        long total = r.orderAmount() + (r.extraCost() != null ? r.extraCost() : 0L);
        return new SettlementOrderResult(
                r.orderId(), r.orderNumber(), counterpartName,
                r.productName(), r.placeName(), r.ordererName(), r.completedAt(),
                r.orderAmount(), r.vendorAmount(), r.extraCost(),
                "NONE", 0L, total, r.vendorFee());
    }

    public static SettlementOrderResult forOrdering(SettlementQueryPort.OrderSettlementRaw r, String counterpartName, Company orderingCompany) {
        long total = r.orderAmount() + (r.extraCost() != null ? r.extraCost() : 0L);
        long vendorFee = r.vendorFee() != null ? r.vendorFee() : 0L;
        long discount = orderingCompany.calcOrderingDiscount(vendorFee);
        return new SettlementOrderResult(
                r.orderId(), r.orderNumber(), counterpartName,
                r.productName(), r.placeName(), r.ordererName(), r.completedAt(),
                r.orderAmount(), r.vendorAmount(), r.extraCost(),
                orderingCompany.getOrderingFeeDiscountType().name(), discount, total - discount, null);
    }
}
