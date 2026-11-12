package com.greenlink.core.application.settlement.dto;

public record SettlementSummaryResult(
        Long companyId,
        String companyName,
        String companyType,
        int count,
        long orderAmountTotal,
        long vendorAmountTotal,
        boolean completed,
        String completedAt
) {}
