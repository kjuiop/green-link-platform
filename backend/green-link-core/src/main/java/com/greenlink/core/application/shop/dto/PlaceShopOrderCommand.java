package com.greenlink.core.application.shop.dto;

import java.util.List;

public record PlaceShopOrderCommand(
        List<Line> items,
        String ordererName,
        String ordererPhone,
        String recipientName,
        String recipientPhone,
        String address,
        String addressDetail,
        String deliveryDate,
        String deliveryTime,
        String eventType,
        String placeName,
        String ribbonLeft,
        String ribbonRight,
        String memo,
        String honeypot
) {
    public record Line(Long productId, int quantity) {}
}
