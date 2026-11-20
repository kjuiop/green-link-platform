package com.greenlink.core.application.taxinvoice.usecase;

import com.greenlink.core.application.taxinvoice.dto.TaxInvoiceResult;

import java.util.Collection;
import java.util.Map;

public interface GetTaxInvoiceStatusUseCase {

    /** 충전 거래 ID별 발행 현황. 발행 기록이 없는 거래는 포함되지 않는다. */
    Map<Long, TaxInvoiceResult> getByPointTransactionIds(Collection<Long> pointTransactionIds);
}
