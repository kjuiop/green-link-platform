package com.greenlink.admin.adapter.out.persistence.order;

import com.greenlink.core.application.order.dto.OrderSearchCondition;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderQueryRepository queryRepository;

    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository, OrderQueryRepository queryRepository) {
        this.jpaRepository = jpaRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id).map(OrderJpaEntity::toDomain);
    }

    @Override
    public List<Order> search(OrderSearchCondition condition) {
        return queryRepository.search(condition).stream()
                .map(OrderJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Order save(Order order) {
        return jpaRepository.save(OrderJpaEntity.from(order)).toDomain();
    }
}
