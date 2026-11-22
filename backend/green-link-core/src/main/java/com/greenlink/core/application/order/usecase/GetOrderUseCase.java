package com.greenlink.core.application.order.usecase;

import com.greenlink.core.application.order.dto.OrderResult;

public interface GetOrderUseCase {

    OrderResult getOrder(Long id);

    OrderResult getOrder(Long id, boolean viewerIsSuperAdmin);
}
