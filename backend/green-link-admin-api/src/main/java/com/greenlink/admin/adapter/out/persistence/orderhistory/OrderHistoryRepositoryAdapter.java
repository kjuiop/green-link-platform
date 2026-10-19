package com.greenlink.admin.adapter.out.persistence.orderhistory;

import com.greenlink.core.domain.orderhistory.model.OrderHistory;
import com.greenlink.core.domain.orderhistory.port.OrderHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderHistoryRepositoryAdapter implements OrderHistoryRepository {

    private final OrderHistoryJpaRepository jpaRepository;

    public OrderHistoryRepositoryAdapter(OrderHistoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrderHistory save(OrderHistory history) {
        return jpaRepository.save(OrderHistoryJpaEntity.from(history)).toDomain();
    }

    @Override
    public List<OrderHistory> findAllByOrderId(Long orderId) {
        return jpaRepository.findAllByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(OrderHistoryJpaEntity::toDomain)
                .toList();
    }
}
