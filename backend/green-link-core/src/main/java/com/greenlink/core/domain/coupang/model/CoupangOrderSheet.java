package com.greenlink.core.domain.coupang.model;

import java.time.LocalDateTime;
import java.util.List;

public record CoupangOrderSheet(
        long orderId,
        long shipmentBoxId,
        String status,
        LocalDateTime orderedAt,
        String ordererName,
        String ordererPhone,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        String receiverAddressDetail,
        String deliveryMessage,
        List<Item> items,
        long totalPrice,
        String rawJson
) {
    public record Item(String name, int quantity, long salesPrice) {}
}
