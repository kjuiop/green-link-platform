package com.greenlink.admin.adapter.out.persistence.orderpriceadjustment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderPriceAdjustmentJpaRepository extends JpaRepository<OrderPriceAdjustmentJpaEntity, Long> {

    List<OrderPriceAdjustmentJpaEntity> findAllByOrderIdOrderByIdDesc(Long orderId);

    Optional<OrderPriceAdjustmentJpaEntity> findByPendingOrderId(Long pendingOrderId);

    List<OrderPriceAdjustmentJpaEntity> findAllByPendingOrderIdIn(Collection<Long> pendingOrderIds);
}
