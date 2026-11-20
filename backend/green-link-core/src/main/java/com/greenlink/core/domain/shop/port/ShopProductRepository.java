package com.greenlink.core.domain.shop.port;

import com.greenlink.core.domain.shop.model.ShopProduct;

import java.util.List;
import java.util.Optional;

public interface ShopProductRepository {

    Optional<ShopProduct> findByIdAndShopId(Long id, Long shopId);

    Optional<ShopProduct> findVisibleByIdAndShopId(Long id, Long shopId);

    List<ShopProduct> findAllByShopId(Long shopId);

    List<ShopProduct> findVisibleByShopId(Long shopId);

    List<ShopProduct> findVisibleByIdInAndShopId(java.util.Collection<Long> ids, Long shopId);

    long countVisibleByShopId(Long shopId);

    int maxSortOrder(Long shopId);

    ShopProduct save(ShopProduct product);

    void delete(ShopProduct product);
}
