package com.greenlink.shopcore.application.auth.usecase;

import com.greenlink.shopcore.application.auth.dto.ShopSession;

public interface ShopLoginUseCase {
    ShopSession login(String loginId, String password);
}
