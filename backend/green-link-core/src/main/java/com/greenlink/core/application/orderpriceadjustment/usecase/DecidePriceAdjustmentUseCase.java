package com.greenlink.core.application.orderpriceadjustment.usecase;

import com.greenlink.core.application.orderpriceadjustment.dto.PriceAdjustmentResult;

public interface DecidePriceAdjustmentUseCase {

    PriceAdjustmentResult accept(Long orderId, Long adjustmentId, boolean proxy);

    PriceAdjustmentResult reject(Long orderId, Long adjustmentId, String reason, boolean proxy);
}
