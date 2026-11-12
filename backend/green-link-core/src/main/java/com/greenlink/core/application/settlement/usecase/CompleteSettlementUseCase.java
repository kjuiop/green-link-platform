package com.greenlink.core.application.settlement.usecase;

import com.greenlink.core.domain.settlement.model.SettlementType;

public interface CompleteSettlementUseCase {
    void completeSettlement(Long companyId, SettlementType type, String fromDate, String toDate, Long adminId);
}
