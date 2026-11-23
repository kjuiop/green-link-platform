package com.greenlink.shop.security;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;

public record ShopPrincipal(Long adminId, ShopRole role, TokenScope scope) {

    public boolean superAdmin() {
        return role == ShopRole.SUPER_ADMIN;
    }
}
