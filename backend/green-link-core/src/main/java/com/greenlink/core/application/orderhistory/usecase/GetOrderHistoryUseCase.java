package com.greenlink.core.application.orderhistory.usecase;

import com.greenlink.core.application.orderhistory.dto.OrderHistoryResult;

import java.util.List;

public interface GetOrderHistoryUseCase {
    List<OrderHistoryResult> getHistory(Long orderId);
}
