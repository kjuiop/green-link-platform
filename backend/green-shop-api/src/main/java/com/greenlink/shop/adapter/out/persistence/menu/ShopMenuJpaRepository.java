package com.greenlink.shop.adapter.out.persistence.menu;

import com.greenlink.shopcore.domain.auth.ShopRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface ShopMenuJpaRepository extends JpaRepository<ShopMenuJpaEntity, Long> {

    @Query("select distinct m from ShopMenuJpaEntity m join m.roles r where r = :role order by m.sortOrder asc")
    List<ShopMenuJpaEntity> findByRole(@Param("role") ShopRole role);
}
