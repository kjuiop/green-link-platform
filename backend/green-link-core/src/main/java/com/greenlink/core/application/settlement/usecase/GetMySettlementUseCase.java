package com.greenlink.core.application.settlement.usecase;

import com.greenlink.core.application.settlement.dto.SettlementOrderResult;
import com.greenlink.core.domain.settlement.model.SettlementType;

import java.util.List;

public interface GetMySettlementUseCase {
    List<SettlementOrderResult> getMySettlement(Long companyId, SettlementType type, String fromDate, String toDate);
}
