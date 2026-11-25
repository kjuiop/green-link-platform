package com.greenlink.shop.adapter.out.persistence.brand;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ShopBrandJpaRepository extends JpaRepository<ShopBrandJpaEntity, Long> {
    List<ShopBrandJpaEntity> findByCompanyIdOrderByIdAsc(Long companyId);
}
