package com.greenlink.shop.security;

import com.greenlink.shopcore.application.common.ManagementActor;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;

public record ShopPrincipal(Long adminId, Long companyId, ShopRole role, TokenScope scope) {

    public ManagementActor toActor() {
        return new ManagementActor(adminId, companyId, role);
    }

    public boolean superAdmin() {
        return role == ShopRole.SUPER_ADMIN;
    }
}
