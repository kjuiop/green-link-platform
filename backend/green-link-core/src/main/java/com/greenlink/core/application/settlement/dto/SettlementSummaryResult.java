package com.greenlink.core.application.settlement.dto;

public record SettlementSummaryResult(
        Long companyId,
        String companyName,
        int count,
        long orderAmountTotal,
        long vendorAmountTotal,
        boolean completed,
        String completedAt
) {}
