package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.domain.brand.model.ShopBrand;

public record BrandResponse(Long id, Long companyId, String name) {

    public static BrandResponse from(ShopBrand b) {
        return new BrandResponse(b.getId(), b.getCompanyId(), b.getName());
    }
}
