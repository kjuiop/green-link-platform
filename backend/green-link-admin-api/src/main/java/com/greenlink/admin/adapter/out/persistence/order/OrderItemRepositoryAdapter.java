package com.greenlink.admin.adapter.out.persistence.order;

import com.greenlink.core.domain.order.model.OrderItem;
import com.greenlink.core.domain.order.port.OrderItemRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderItemRepositoryAdapter implements OrderItemRepository {

    private final OrderItemJpaRepository jpaRepository;

    public OrderItemRepositoryAdapter(OrderItemJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<OrderItem> findAllByOrderId(Long orderId) {
        return findAllByOrderIds(List.of(orderId)).getOrDefault(orderId, List.of());
    }

    @Override
    public Map<Long, List<OrderItem>> findAllByOrderIds(List<Long> orderIds) {
        Map<Long, List<OrderItem>> result = new LinkedHashMap<>();
        if (orderIds == null || orderIds.isEmpty()) return result;
        for (OrderItemJpaEntity e : jpaRepository.findAllByOrderIdInOrderByOrderIdAscSortOrderAscIdAsc(orderIds)) {
            result.computeIfAbsent(e.getOrderId(), k -> new ArrayList<>()).add(e.toDomain());
        }
        return result;
    }

    @Override
    public void replaceAll(Long orderId, List<OrderItem> items) {
        jpaRepository.deleteAllByOrderId(orderId);
        List<OrderItemJpaEntity> entities = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            entities.add(OrderItemJpaEntity.of(orderId, items.get(i), i));
        }
        jpaRepository.saveAll(entities);
    }
}
