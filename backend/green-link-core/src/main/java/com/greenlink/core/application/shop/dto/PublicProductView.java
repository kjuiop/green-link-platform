package com.greenlink.core.application.shop.dto;

import com.greenlink.core.domain.shop.model.ShopProduct;

/** 고객에게 보여도 되는 필드만 담는다. 회사·쇼핑몰 내부 ID는 넣지 않는다. */
public record PublicProductView(
        Long id,
        String category,
        String categoryLabel,
        String name,
        String description,
        long price,
        String imageUrl
) {
    public static PublicProductView from(ShopProduct p) {
        return new PublicProductView(p.getId(), p.getCategory().name(), p.getCategory().getLabel(), p.getName(),
                p.getDescription(), p.getPrice(), p.getImageUrl());
    }
}
