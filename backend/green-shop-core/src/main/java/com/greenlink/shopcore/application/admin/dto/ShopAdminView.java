package com.greenlink.shopcore.application.admin.dto;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;

import java.time.LocalDateTime;

public record ShopAdminView(Long id, String loginId, String name, String companyName, ShopRole role,
                            ShopAdminStatus status, LocalDateTime requestedAt, LocalDateTime processedAt,
                            String rejectReason) {

    public static ShopAdminView from(ShopAdminMember m) {
        return new ShopAdminView(m.getId(), m.getLoginId(), m.getName(), m.getCompanyName(), ShopRole.SHOP_ADMIN,
            m.getStatus(), m.getRequestedAt(), m.getProcessedAt(), m.getRejectReason());
    }
}
