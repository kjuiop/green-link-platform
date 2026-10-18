package com.greenlink.core.application.order.usecase;

import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderResult;

public interface CreateOrderUseCase {

    OrderResult createOrder(CreateOrderCommand cmd);
}
