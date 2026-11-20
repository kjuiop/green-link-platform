package com.greenlink.core.application.shop.dto;

import com.greenlink.core.domain.shop.model.ShopProduct;

public record ShopProductResult(
        Long id,
        String category,
        String categoryLabel,
        String name,
        String description,
        long price,
        String imageUrl,
        boolean visible,
        int sortOrder
) {
    public static ShopProductResult from(ShopProduct p) {
        return new ShopProductResult(p.getId(), p.getCategory().name(), p.getCategory().getLabel(), p.getName(),
                p.getDescription(), p.getPrice(), p.getImageUrl(), p.isVisible(), p.getSortOrder());
    }
}
