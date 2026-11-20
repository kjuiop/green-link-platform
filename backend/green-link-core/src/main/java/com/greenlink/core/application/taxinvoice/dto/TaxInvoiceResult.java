package com.greenlink.core.application.taxinvoice.dto;

import com.greenlink.core.domain.taxinvoice.model.TaxInvoice;

public record TaxInvoiceResult(
        Long pointTransactionId,
        String status,
        String failReason,
        String recipientEmail
) {
    public static TaxInvoiceResult from(TaxInvoice t) {
        return new TaxInvoiceResult(t.getPointTransactionId(), t.getStatus().name(), t.getFailReason(), t.getRecipientEmail());
    }
}
