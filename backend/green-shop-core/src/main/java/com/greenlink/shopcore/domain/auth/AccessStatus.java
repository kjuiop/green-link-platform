package com.greenlink.shopcore.domain.auth;

import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;

public enum AccessStatus {
    NONE,
    PENDING,
    APPROVED,
    REJECTED,
    SITE_DISABLED;

    public static AccessStatus from(ShopAdminStatus status) {
        return switch (status) {
            case PENDING -> PENDING;
            case APPROVED -> APPROVED;
            case REJECTED -> REJECTED;
        };
    }
}
