package com.greenlink.core.application.orderhistory.dto;

import com.greenlink.core.domain.orderhistory.model.OrderHistoryAction;

public record OrderHistoryResult(
        Long id,
        Long orderId,
        OrderHistoryAction action,
        String detail,
        String createdAt
) {}
