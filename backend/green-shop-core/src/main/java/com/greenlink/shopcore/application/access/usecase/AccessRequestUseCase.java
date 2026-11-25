package com.greenlink.shopcore.application.access.usecase;

import com.greenlink.shopcore.application.access.dto.MyAccess;
import com.greenlink.shopcore.domain.auth.ShopRole;

public interface AccessRequestUseCase {
    MyAccess getMyAccess(Long adminId, boolean superAdmin);
    MyAccess apply(Long adminId, boolean superAdmin, ShopRole requestedRole);
}
