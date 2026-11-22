package com.greenlink.core.domain.shop.port;

import com.greenlink.core.domain.shop.model.Shop;

import java.util.Optional;

public interface ShopRepository {

    Optional<Shop> findById(Long id);

    Optional<Shop> findByCompanyId(Long companyId);

    Optional<Shop> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Shop save(Shop shop);
}
