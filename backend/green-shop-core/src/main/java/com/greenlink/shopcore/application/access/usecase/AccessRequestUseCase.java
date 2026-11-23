package com.greenlink.shopcore.application.access.usecase;

import com.greenlink.shopcore.application.access.dto.MyAccess;

public interface AccessRequestUseCase {
    MyAccess getMyAccess(Long adminId, boolean superAdmin);
    MyAccess apply(Long adminId, boolean superAdmin);
}
