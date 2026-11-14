package com.greenlink.core.application.order.dto;

public record OrderItemCommand(
        Long productId,
        String productName,
        long unitPrice,
        int quantity
) {}
