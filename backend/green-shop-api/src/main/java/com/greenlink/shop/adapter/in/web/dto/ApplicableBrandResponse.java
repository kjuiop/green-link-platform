package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.access.dto.ApplicableBrand;

public record ApplicableBrandResponse(Long id, String name) {

    public static ApplicableBrandResponse from(ApplicableBrand b) {
        return new ApplicableBrandResponse(b.id(), b.name());
    }
}
