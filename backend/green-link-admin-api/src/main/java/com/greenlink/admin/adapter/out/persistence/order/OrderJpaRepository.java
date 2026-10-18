package com.greenlink.admin.adapter.out.persistence.order;

import com.greenlink.core.domain.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    List<OrderJpaEntity> findByStatus(OrderStatus status);

    List<OrderJpaEntity> findByVendorId(Long vendorId);

    List<OrderJpaEntity> findByStatusAndVendorId(OrderStatus status, Long vendorId);
}
