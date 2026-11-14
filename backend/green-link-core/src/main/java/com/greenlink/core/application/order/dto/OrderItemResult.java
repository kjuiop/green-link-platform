package com.greenlink.core.application.order.dto;

public record OrderItemResult(
        Long id,
        Long productId,
        String productName,
        long unitPrice,
        int quantity,
        long amount
) {}
