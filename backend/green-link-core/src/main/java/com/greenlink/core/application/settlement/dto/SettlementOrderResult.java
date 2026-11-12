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
        Long vendorAmount
) {}
