package com.greenlink.shopcore.application.auth.usecase;

import com.greenlink.shopcore.application.auth.dto.ShopSession;

public interface RefreshTokenUseCase {
    ShopSession refresh(Long adminId);
}
