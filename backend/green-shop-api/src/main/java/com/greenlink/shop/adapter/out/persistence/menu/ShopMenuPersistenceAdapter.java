package com.greenlink.shop.adapter.out.persistence.menu;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.menu.model.ShopMenu;
import com.greenlink.shopcore.domain.menu.port.ShopMenuRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class ShopMenuPersistenceAdapter implements ShopMenuRepository {

    private final ShopMenuJpaRepository jpaRepository;

    ShopMenuPersistenceAdapter(ShopMenuJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ShopMenu> findByRole(ShopRole role) {
        return jpaRepository.findByRole(role).stream()
            .map(ShopMenuJpaEntity::toDomain).toList();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public ShopMenu save(ShopMenu menu) {
        return jpaRepository.save(ShopMenuJpaEntity.from(menu)).toDomain();
    }
}
