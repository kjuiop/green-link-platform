package com.greenlink.shopcore.application.access.usecase;

import com.greenlink.shopcore.application.access.dto.ApplicableBrand;
import com.greenlink.shopcore.application.access.dto.MyAccess;
import com.greenlink.shopcore.domain.auth.ShopRole;

import java.util.List;
import java.util.Set;

public interface AccessRequestUseCase {
    MyAccess getMyAccess(Long adminId, boolean superAdmin);
    MyAccess apply(Long adminId, boolean superAdmin, ShopRole requestedRole, Set<Long> requestedBrandIds);
    List<ApplicableBrand> getApplicableBrands(Long adminId, boolean superAdmin);
}
