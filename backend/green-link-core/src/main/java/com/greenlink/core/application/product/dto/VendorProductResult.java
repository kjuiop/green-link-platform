package com.greenlink.core.application.product.dto;

public record VendorProductResult(
        Long id,
        String name,
        long price,
        String memo,
        int sortOrder,
        boolean enabled,
        Long baseProductId,
        Long basePrice,
        String createdAt,
        String category) {}
