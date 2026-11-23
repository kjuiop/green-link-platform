package com.greenlink.core.application.orderpriceadjustment.usecase;

import com.greenlink.core.application.orderpriceadjustment.dto.PriceAdjustmentResult;

import java.util.List;

public interface GetPriceAdjustmentsUseCase {

    List<PriceAdjustmentResult> getAdjustments(Long orderId);
}
