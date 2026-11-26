package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.admin.dto.ShopAdminView;

import java.util.List;

public record ShopAdminResponse(Long id, Long adminId, Long companyId, String loginId, String name,
                                String companyName, String role, String requestedRole, String status,
                                String requestedAt, String processedAt, String rejectReason,
                                List<Long> brandIds, List<Long> requestedBrandIds, boolean passwordSetupPending) {

    public static ShopAdminResponse from(ShopAdminView v) {
        return new ShopAdminResponse(v.id(), v.adminId(), v.companyId(), v.loginId(), v.name(), v.companyName(),
            v.role() == null ? null : v.role().name(), v.requestedRole().name(), v.status().name(),
            v.requestedAt().toString(), v.processedAt() == null ? null : v.processedAt().toString(),
            v.rejectReason(), v.brandIds().stream().sorted().toList(),
            v.requestedBrandIds().stream().sorted().toList(), v.passwordSetupPending());
    }
}
