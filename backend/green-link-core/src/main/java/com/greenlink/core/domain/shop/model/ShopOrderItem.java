package com.greenlink.core.domain.shop.model;

public record ShopOrderItem(Long productId, String name, long unitPrice, int quantity) {

    public long amount() {
        return unitPrice * quantity;
    }
}
