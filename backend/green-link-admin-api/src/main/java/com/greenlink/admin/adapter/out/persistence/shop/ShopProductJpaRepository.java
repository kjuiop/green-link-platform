package com.greenlink.admin.adapter.out.persistence.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShopProductJpaRepository extends JpaRepository<ShopProductJpaEntity, Long> {

    Optional<ShopProductJpaEntity> findByIdAndShopId(Long id, Long shopId);

    Optional<ShopProductJpaEntity> findByIdAndShopIdAndVisibleTrue(Long id, Long shopId);

    List<ShopProductJpaEntity> findByShopIdOrderBySortOrderAscIdAsc(Long shopId);

    List<ShopProductJpaEntity> findByShopIdAndVisibleTrueOrderBySortOrderAscIdAsc(Long shopId);

    List<ShopProductJpaEntity> findByIdInAndShopIdAndVisibleTrue(Collection<Long> ids, Long shopId);

    long countByShopIdAndVisibleTrue(Long shopId);

    @Query("select coalesce(max(p.sortOrder), -1) from ShopProductJpaEntity p where p.shopId = :shopId")
    int maxSortOrder(@Param("shopId") Long shopId);
}
