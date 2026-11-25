package com.greenlink.shop.adapter.out.persistence.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface ShopAdminMemberJpaRepository extends JpaRepository<ShopAdminMemberJpaEntity, Long> {
    Optional<ShopAdminMemberJpaEntity> findByAdminId(Long adminId);
    List<ShopAdminMemberJpaEntity> findByStatusOrderByRequestedAtDesc(
        com.greenlink.shopcore.domain.member.model.ShopAdminStatus status);
    List<ShopAdminMemberJpaEntity> findAllByOrderByRequestedAtDesc();
}
