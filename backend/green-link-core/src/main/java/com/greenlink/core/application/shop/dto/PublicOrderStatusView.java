package com.greenlink.core.application.shop.dto;

import com.greenlink.core.domain.shop.model.ShopOrder;

import java.time.LocalDateTime;
import java.util.List;

/** 주문 조회 응답. 주소·연락처는 다시 내려주지 않는다. */
public record PublicOrderStatusView(
        String orderNo,
        String status,
        List<Item> items,
        long totalPrice,
        String deliveryDate,
        LocalDateTime createdAt
) {
    public record Item(String name, long unitPrice, int quantity) {}

    public static PublicOrderStatusView from(ShopOrder o) {
        return new PublicOrderStatusView(o.getOrderNo(), o.getStatus().name(),
                o.getItems().stream().map(i -> new Item(i.name(), i.unitPrice(), i.quantity())).toList(),
                o.getTotalPrice(), o.getDeliveryDate(), o.getCreatedAt());
    }
}
