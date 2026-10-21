package com.greenlink.admin.adapter.out.persistence.orderhistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderHistoryJpaRepository extends JpaRepository<OrderHistoryJpaEntity, Long> {
    List<OrderHistoryJpaEntity> findAllByOrderIdOrderByCreatedAtDesc(Long orderId);
}
