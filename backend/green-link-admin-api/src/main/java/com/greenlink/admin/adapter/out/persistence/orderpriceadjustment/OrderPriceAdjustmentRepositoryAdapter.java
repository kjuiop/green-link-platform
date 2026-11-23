package com.greenlink.admin.adapter.out.persistence.orderpriceadjustment;

import com.greenlink.core.domain.orderpriceadjustment.model.OrderPriceAdjustment;
import com.greenlink.core.domain.orderpriceadjustment.port.OrderPriceAdjustmentRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class OrderPriceAdjustmentRepositoryAdapter implements OrderPriceAdjustmentRepository {

    private final OrderPriceAdjustmentJpaRepository jpaRepository;

    public OrderPriceAdjustmentRepositoryAdapter(OrderPriceAdjustmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrderPriceAdjustment save(OrderPriceAdjustment adjustment) {
        return jpaRepository.saveAndFlush(OrderPriceAdjustmentJpaEntity.from(adjustment)).toDomain();
    }

    @Override
    public Optional<OrderPriceAdjustment> findById(Long id) {
        return jpaRepository.findById(id).map(OrderPriceAdjustmentJpaEntity::toDomain);
    }

    @Override
    public List<OrderPriceAdjustment> findAllByOrderId(Long orderId) {
        return jpaRepository.findAllByOrderIdOrderByIdDesc(orderId).stream()
                .map(OrderPriceAdjustmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<OrderPriceAdjustment> findPendingByOrderId(Long orderId) {
        return jpaRepository.findByPendingOrderId(orderId).map(OrderPriceAdjustmentJpaEntity::toDomain);
    }

    @Override
    public Map<Long, OrderPriceAdjustment> findPendingByOrderIds(Collection<Long> orderIds) {
        if (orderIds.isEmpty()) return Map.of();
        return jpaRepository.findAllByPendingOrderIdIn(orderIds).stream()
                .map(OrderPriceAdjustmentJpaEntity::toDomain)
                .collect(Collectors.toMap(OrderPriceAdjustment::getOrderId, Function.identity(), (a, b) -> a));
    }
}
