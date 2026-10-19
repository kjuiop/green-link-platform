package com.greenlink.core.application.order.usecase;

import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.application.order.dto.OrderSearchCondition;

import java.util.List;

public interface GetOrdersUseCase {

    List<OrderResult> getOrders(OrderSearchCondition condition);
}
