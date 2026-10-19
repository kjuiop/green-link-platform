package com.greenlink.core.application.orderhistory.usecase;

import com.greenlink.core.domain.orderhistory.model.OrderHistoryAction;

public interface RecordOrderHistoryUseCase {
    void record(Long orderId, OrderHistoryAction action, String detail);
}
