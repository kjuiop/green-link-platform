package com.greenlink.shopcore.application.admin.dto;

import com.greenlink.shopcore.application.auth.dto.AccountProfile;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record ShopAdminView(Long id, Long adminId, Long companyId, String loginId, String name, String companyName,
                            ShopRole role, ShopRole requestedRole, ShopAdminStatus status,
                            LocalDateTime requestedAt, LocalDateTime processedAt, String rejectReason,
                            Set<Long> brandIds, boolean passwordSetupPending) {

    public static ShopAdminView of(ShopAdminMember m, AccountProfile profile, String fallbackCompanyName,
                                    Set<Long> brandIds) {
        return new ShopAdminView(m.getId(), m.getAdminId(), m.getCompanyId(),
            profile == null ? null : profile.loginId(),
            profile == null ? null : profile.name(),
            profile != null && profile.companyName() != null ? profile.companyName() : fallbackCompanyName,
            m.getRole(), m.getRequestedRole(), m.getStatus(),
            m.getRequestedAt(), m.getProcessedAt(), m.getRejectReason(),
            brandIds == null ? Set.of() : brandIds,
            profile != null && profile.passwordSetupPending());
    }
}
