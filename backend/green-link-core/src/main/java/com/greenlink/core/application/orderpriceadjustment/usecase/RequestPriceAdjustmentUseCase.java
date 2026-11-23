package com.greenlink.core.application.orderpriceadjustment.usecase;

import com.greenlink.core.application.orderpriceadjustment.dto.PriceAdjustmentResult;

public interface RequestPriceAdjustmentUseCase {

    PriceAdjustmentResult request(Long orderId, long newTotalAmount, String reason, boolean proxy);
}
