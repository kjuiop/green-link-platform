package com.greenlink.core.application.taxinvoice.dto;

import java.util.List;

public record TaxInvoicePreviewResult(
        Long pointTransactionId,
        String itemName,
        long supplyAmount,
        long taxAmount,
        long totalAmount,
        PartyInfo supplier,
        PartyInfo recipient,
        String status,
        String failReason,
        List<String> missing
) {
    public record PartyInfo(String corpNum, String companyName, String ceoName, String address, String email) {}
}
