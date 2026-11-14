package com.greenlink.core.domain.order.port;

import com.greenlink.core.domain.order.model.OrderItem;

import java.util.List;
import java.util.Map;

public interface OrderItemRepository {

    List<OrderItem> findAllByOrderId(Long orderId);

    Map<Long, List<OrderItem>> findAllByOrderIds(List<Long> orderIds);

    void replaceAll(Long orderId, List<OrderItem> items);
}
