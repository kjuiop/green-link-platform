package com.greenlink.shopcore.application.auth.port;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;

public interface AccessTokenIssuer {
    String issue(Long adminId, ShopRole role, TokenScope scope);
}
