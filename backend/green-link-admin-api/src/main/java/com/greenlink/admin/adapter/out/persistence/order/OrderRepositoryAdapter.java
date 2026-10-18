package com.greenlink.admin.adapter.out.persistence.order;

import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.greenlink.core.domain.order.port.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id).map(OrderJpaEntity::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll().stream()
                .map(OrderJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Order> findAllByStatus(OrderStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(OrderJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Order> findAllByVendorId(Long vendorId) {
        return jpaRepository.findByVendorId(vendorId).stream()
                .map(OrderJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Order save(Order order) {
        return jpaRepository.save(OrderJpaEntity.from(order)).toDomain();
    }
}
