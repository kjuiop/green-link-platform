package com.greenlink.core.domain.orderpriceadjustment.port;

import com.greenlink.core.domain.orderpriceadjustment.model.OrderPriceAdjustment;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderPriceAdjustmentRepository {

    OrderPriceAdjustment save(OrderPriceAdjustment adjustment);

    Optional<OrderPriceAdjustment> findById(Long id);

    List<OrderPriceAdjustment> findAllByOrderId(Long orderId);

    Optional<OrderPriceAdjustment> findPendingByOrderId(Long orderId);

    Map<Long, OrderPriceAdjustment> findPendingByOrderIds(Collection<Long> orderIds);
}
