package com.greenlink.shopcore.application.brand.usecase;

import com.greenlink.shopcore.domain.auth.ShopRole;

public interface BrandAccessUseCase {
    void requireAccess(Long adminId, ShopRole role, Long companyId, Long brandId);
}
