package com.greenlink.core.domain.order.port;

import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(Long id);

    List<Order> findAll();

    List<Order> findAllByStatus(OrderStatus status);

    List<Order> findAllByVendorId(Long vendorId);

    Order save(Order order);
}
