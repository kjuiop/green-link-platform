package com.greenlink.core.application.settlement.usecase;

import com.greenlink.core.application.settlement.dto.AdminSettlementQuery;
import com.greenlink.core.application.settlement.dto.SettlementSummaryResult;

import java.util.List;

public interface GetAdminSettlementsUseCase {
    List<SettlementSummaryResult> getAdminSettlements(AdminSettlementQuery query);
}
