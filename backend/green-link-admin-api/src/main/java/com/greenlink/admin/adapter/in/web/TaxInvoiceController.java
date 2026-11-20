package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.IssueTaxInvoiceRequest;
import com.greenlink.core.application.taxinvoice.dto.TaxInvoicePreviewResult;
import com.greenlink.core.application.taxinvoice.dto.TaxInvoiceResult;
import com.greenlink.core.application.taxinvoice.usecase.IssueTaxInvoiceUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/points/transactions/{transactionId}/tax-invoice")
public class TaxInvoiceController {

    private final IssueTaxInvoiceUseCase issueTaxInvoiceUseCase;

    public TaxInvoiceController(IssueTaxInvoiceUseCase issueTaxInvoiceUseCase) {
        this.issueTaxInvoiceUseCase = issueTaxInvoiceUseCase;
    }

    @GetMapping("/preview")
    public ResponseEntity<TaxInvoicePreviewResult> preview(@PathVariable Long transactionId) {
        return ResponseEntity.ok(issueTaxInvoiceUseCase.preview(transactionId));
    }

    @PostMapping
    public ResponseEntity<TaxInvoiceResult> issue(@PathVariable Long transactionId,
                                                  @RequestBody IssueTaxInvoiceRequest req,
                                                  Authentication auth) {
        Long adminId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(issueTaxInvoiceUseCase.issue(transactionId, req.recipientEmail(), adminId));
    }
}
