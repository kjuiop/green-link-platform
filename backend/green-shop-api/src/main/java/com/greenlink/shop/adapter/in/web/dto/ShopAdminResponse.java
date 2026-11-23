package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.admin.dto.ShopAdminView;

public record ShopAdminResponse(Long id, String loginId, String name, String companyName, String role,
                                String status, String requestedAt, String processedAt, String rejectReason) {

    public static ShopAdminResponse from(ShopAdminView v) {
        return new ShopAdminResponse(v.id(), v.loginId(), v.name(), v.companyName(), v.role().name(),
            v.status().name(), v.requestedAt().toString(),
            v.processedAt() == null ? null : v.processedAt().toString(), v.rejectReason());
    }
}
