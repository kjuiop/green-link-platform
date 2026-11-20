package com.greenlink.core.application.taxinvoice.usecase;

import com.greenlink.core.application.taxinvoice.dto.TaxInvoicePreviewResult;
import com.greenlink.core.application.taxinvoice.dto.TaxInvoiceResult;

public interface IssueTaxInvoiceUseCase {

    TaxInvoicePreviewResult preview(Long pointTransactionId);

    /** 발행하거나 실패 건을 재시도한다. 바로빌 호출 결과(성공/실패)는 반환값의 status로 전달한다. */
    TaxInvoiceResult issue(Long pointTransactionId, String recipientEmail, Long adminId);
}
