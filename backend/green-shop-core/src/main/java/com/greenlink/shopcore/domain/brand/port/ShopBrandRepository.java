package com.greenlink.shopcore.domain.brand.port;

import com.greenlink.shopcore.domain.brand.model.ShopBrand;

import java.util.List;
import java.util.Optional;

public interface ShopBrandRepository {
    Optional<ShopBrand> findById(Long id);
    List<ShopBrand> findAll();
    List<ShopBrand> findAllByCompanyId(Long companyId);
    boolean existsByCompanyIdAndName(Long companyId, String name);
    ShopBrand save(ShopBrand brand);
}
