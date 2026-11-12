package com.greenlink.core.application.settlement.usecase;

import com.greenlink.core.application.settlement.dto.AdminSettlementQuery;
import com.greenlink.core.application.settlement.dto.SettlementOrderResult;
import com.greenlink.core.domain.settlement.model.SettlementType;

import java.util.List;

public interface GetAdminSettlementDetailUseCase {
    List<SettlementOrderResult> getAdminSettlementDetail(Long companyId, AdminSettlementQuery query);
}
