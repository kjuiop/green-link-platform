package com.greenlink.core.application.order.usecase;

import com.greenlink.core.application.order.dto.OrderResult;

import java.util.List;

public interface GetOrdersUseCase {

    List<OrderResult> getOrders(String status, Long vendorId);
}
