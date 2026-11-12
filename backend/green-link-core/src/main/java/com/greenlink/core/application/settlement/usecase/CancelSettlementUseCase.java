package com.greenlink.core.application.settlement.usecase;

import com.greenlink.core.domain.settlement.model.SettlementType;

public interface CancelSettlementUseCase {
    void cancelSettlement(Long companyId, SettlementType type, String fromDate, String toDate);
}
