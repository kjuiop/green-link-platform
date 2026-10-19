package com.greenlink.core.application.order.usecase;

import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.application.order.dto.UpdateOrderCommand;

public interface UpdateOrderUseCase {

    OrderResult updateOrder(Long id, UpdateOrderCommand cmd);
}
