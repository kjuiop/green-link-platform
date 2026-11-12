package com.greenlink.core.application.settlement.usecase;

import com.greenlink.core.application.settlement.dto.SettlementOrderResult;

import java.util.List;

public interface ExportSettlementExcelUseCase {
    byte[] export(List<SettlementOrderResult> orders);
}
