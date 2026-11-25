package com.greenlink.shop.adapter.out.persistence.brand;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ShopBrandMemberJpaRepository extends JpaRepository<ShopBrandMemberJpaEntity, Long> {
    List<ShopBrandMemberJpaEntity> findByMemberId(Long memberId);
    boolean existsByBrandIdAndMemberId(Long brandId, Long memberId);
}
