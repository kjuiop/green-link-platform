package com.greenlink.shop.adapter.out.persistence.brand;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface ShopBrandJpaRepository extends JpaRepository<ShopBrandJpaEntity, Long> {
    Optional<ShopBrandJpaEntity> findByIdAndDeletedAtIsNull(Long id);
    List<ShopBrandJpaEntity> findByDeletedAtIsNullOrderByIdAsc();
    List<ShopBrandJpaEntity> findByCompanyIdAndDeletedAtIsNullOrderByIdAsc(Long companyId);
    boolean existsByCompanyIdAndNameAndDeletedAtIsNull(Long companyId, String name);
}
