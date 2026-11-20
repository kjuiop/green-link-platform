package com.greenlink.admin.adapter.out.persistence.shop;

import com.greenlink.core.domain.shop.model.Shop;
import com.greenlink.core.domain.shop.port.ShopRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ShopRepositoryAdapter implements ShopRepository {

    private final ShopJpaRepository jpaRepository;

    public ShopRepositoryAdapter(ShopJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Shop> findById(Long id) {
        return jpaRepository.findById(id).map(ShopJpaEntity::toDomain);
    }

    @Override
    public Optional<Shop> findByCompanyId(Long companyId) {
        return jpaRepository.findByCompanyId(companyId).map(ShopJpaEntity::toDomain);
    }

    @Override
    public Optional<Shop> findBySlug(String slug) {
        return jpaRepository.findBySlug(slug).map(ShopJpaEntity::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }

    @Override
    public Shop save(Shop shop) {
        ShopJpaEntity entity = shop.getId() == null
                ? ShopJpaEntity.from(shop)
                : jpaRepository.findById(shop.getId()).orElseGet(() -> ShopJpaEntity.from(shop));
        entity.apply(shop);
        return jpaRepository.saveAndFlush(entity).toDomain();
    }
}
