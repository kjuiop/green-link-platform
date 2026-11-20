package com.greenlink.core.application.shop.dto;

import com.greenlink.core.domain.shop.model.ShopProductCategory;

public record SaveShopProductCommand(
        ShopProductCategory category,
        String name,
        String description,
        long price,
        String imageUrl,
        boolean visible
) {}
