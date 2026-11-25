package com.greenlink.shopcore.domain.brand.port;

import com.greenlink.shopcore.domain.brand.model.ShopBrand;

import java.util.List;
import java.util.Optional;

public interface ShopBrandRepository {
    Optional<ShopBrand> findById(Long id);
    List<ShopBrand> findAllByCompanyId(Long companyId);
    ShopBrand save(ShopBrand brand);
}
