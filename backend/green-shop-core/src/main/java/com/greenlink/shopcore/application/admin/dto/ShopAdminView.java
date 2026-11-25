package com.greenlink.shopcore.application.admin.dto;

import com.greenlink.shopcore.application.auth.dto.AccountProfile;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;

import java.time.LocalDateTime;

public record ShopAdminView(Long id, Long adminId, Long companyId, String loginId, String name, String companyName,
                            ShopRole role, ShopRole requestedRole, ShopAdminStatus status,
                            LocalDateTime requestedAt, LocalDateTime processedAt, String rejectReason) {

    public static ShopAdminView of(ShopAdminMember m, AccountProfile profile, String fallbackCompanyName) {
        return new ShopAdminView(m.getId(), m.getAdminId(), m.getCompanyId(),
            profile == null ? null : profile.loginId(),
            profile == null ? null : profile.name(),
            profile != null && profile.companyName() != null ? profile.companyName() : fallbackCompanyName,
            m.getRole(), m.getRequestedRole(), m.getStatus(),
            m.getRequestedAt(), m.getProcessedAt(), m.getRejectReason());
    }
}
