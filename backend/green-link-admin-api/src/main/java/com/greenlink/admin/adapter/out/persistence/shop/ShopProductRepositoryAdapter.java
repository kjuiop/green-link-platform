package com.greenlink.admin.adapter.out.persistence.shop;

import com.greenlink.core.domain.shop.model.ShopProduct;
import com.greenlink.core.domain.shop.port.ShopProductRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class ShopProductRepositoryAdapter implements ShopProductRepository {

    private final ShopProductJpaRepository jpaRepository;

    public ShopProductRepositoryAdapter(ShopProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ShopProduct> findByIdAndShopId(Long id, Long shopId) {
        return jpaRepository.findByIdAndShopId(id, shopId).map(ShopProductJpaEntity::toDomain);
    }

    @Override
    public Optional<ShopProduct> findVisibleByIdAndShopId(Long id, Long shopId) {
        return jpaRepository.findByIdAndShopIdAndVisibleTrue(id, shopId).map(ShopProductJpaEntity::toDomain);
    }

    @Override
    public List<ShopProduct> findAllByShopId(Long shopId) {
        return jpaRepository.findByShopIdOrderBySortOrderAscIdAsc(shopId).stream()
                .map(ShopProductJpaEntity::toDomain).toList();
    }

    @Override
    public List<ShopProduct> findVisibleByShopId(Long shopId) {
        return jpaRepository.findByShopIdAndVisibleTrueOrderBySortOrderAscIdAsc(shopId).stream()
                .map(ShopProductJpaEntity::toDomain).toList();
    }

    @Override
    public List<ShopProduct> findVisibleByIdInAndShopId(Collection<Long> ids, Long shopId) {
        return jpaRepository.findByIdInAndShopIdAndVisibleTrue(ids, shopId).stream()
                .map(ShopProductJpaEntity::toDomain).toList();
    }

    @Override
    public long countVisibleByShopId(Long shopId) {
        return jpaRepository.countByShopIdAndVisibleTrue(shopId);
    }

    @Override
    public int maxSortOrder(Long shopId) {
        return jpaRepository.maxSortOrder(shopId);
    }

    @Override
    public ShopProduct save(ShopProduct product) {
        ShopProductJpaEntity entity = product.getId() == null
                ? ShopProductJpaEntity.from(product)
                : jpaRepository.findById(product.getId()).orElseGet(() -> ShopProductJpaEntity.from(product));
        entity.apply(product);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public void delete(ShopProduct product) {
        jpaRepository.deleteById(product.getId());
    }
}
