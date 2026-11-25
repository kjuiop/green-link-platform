package com.greenlink.shopcore.application.brand.usecase;

import com.greenlink.shopcore.application.common.ManagementActor;
import com.greenlink.shopcore.domain.brand.model.ShopBrand;

import java.util.List;

public interface BrandManagementUseCase {
    List<ShopBrand> list(ManagementActor actor, Long companyId);
    ShopBrand create(ManagementActor actor, Long companyId, String name);
    ShopBrand rename(ManagementActor actor, Long brandId, String name);
    void delete(ManagementActor actor, Long brandId);
}
