package com.greenlink.core.application.settlement.dto;

import java.util.List;

public record MySettlementResult(
        String companyType,
        List<SettlementOrderResult> vendorOrders,
        List<SettlementOrderResult> orderingOrders
) {}
