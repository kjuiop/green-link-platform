package com.greenlink.admin.adapter.out.persistence.shop;

import com.greenlink.core.domain.shop.model.ShopOrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShopOrderJpaRepository extends JpaRepository<ShopOrderJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from ShopOrderJpaEntity o where o.id = :id")
    Optional<ShopOrderJpaEntity> findByIdForUpdate(@Param("id") Long id);

    Optional<ShopOrderJpaEntity> findByShopIdAndOrderNo(Long shopId, String orderNo);

    boolean existsByOrderNo(String orderNo);

    Page<ShopOrderJpaEntity> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);

    Page<ShopOrderJpaEntity> findByCompanyIdOrderByCreatedAtDescIdDesc(Long companyId, Pageable pageable);

    Page<ShopOrderJpaEntity> findByStatusOrderByCreatedAtDescIdDesc(ShopOrderStatus status, Pageable pageable);

    Page<ShopOrderJpaEntity> findByCompanyIdAndStatusOrderByCreatedAtDescIdDesc(
            Long companyId, ShopOrderStatus status, Pageable pageable);

    long countByStatus(ShopOrderStatus status);

    long countByCompanyIdAndStatus(Long companyId, ShopOrderStatus status);
}
