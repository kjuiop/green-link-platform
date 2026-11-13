package com.greenlink.core.application.settlement.usecase;

import com.greenlink.core.application.settlement.dto.MySettlementResult;

public interface GetMySettlementUseCase {
    MySettlementResult getMySettlement(Long companyId, String fromDate, String toDate);
}
