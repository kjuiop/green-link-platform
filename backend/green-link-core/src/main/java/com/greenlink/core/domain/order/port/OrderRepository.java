package com.greenlink.core.domain.order.port;

import com.greenlink.core.application.order.dto.OrderSearchCondition;
import com.greenlink.core.domain.order.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(Long id);

    List<Order> search(OrderSearchCondition condition);

    Order save(Order order);
}
