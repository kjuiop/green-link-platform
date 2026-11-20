package com.greenlink.admin.adapter.out.persistence.shop;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopJpaRepository extends JpaRepository<ShopJpaEntity, Long> {

    Optional<ShopJpaEntity> findByCompanyId(Long companyId);

    Optional<ShopJpaEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
