package com.greenlink.core.domain.orderhistory.port;

import com.greenlink.core.domain.orderhistory.model.OrderHistory;

import java.util.List;

public interface OrderHistoryRepository {
    OrderHistory save(OrderHistory history);
    List<OrderHistory> findAllByOrderId(Long orderId);
}
