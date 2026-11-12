package com.greenlink.core.application.settlement.dto;

import com.greenlink.core.domain.settlement.model.SettlementType;

public record AdminSettlementQuery(
        String fromDate,
        String toDate,
        SettlementType type
) {}
