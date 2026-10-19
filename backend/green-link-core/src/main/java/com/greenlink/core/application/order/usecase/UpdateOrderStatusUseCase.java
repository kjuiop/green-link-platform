package com.greenlink.core.application.order.usecase;

import com.greenlink.core.application.order.dto.OrderResult;

public interface UpdateOrderStatusUseCase {

    OrderResult updateOrderStatus(Long id, String action);
}
