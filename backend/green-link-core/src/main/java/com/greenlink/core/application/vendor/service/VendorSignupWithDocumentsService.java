package com.greenlink.core.application.vendor.service;

import com.greenlink.core.application.vendor.dto.SignupDocument;
import com.greenlink.core.application.vendor.dto.VendorResult;
import com.greenlink.core.application.vendor.dto.VendorSignupCommand;
import com.greenlink.core.application.vendor.dto.VendorSignupWithDocumentsResult;
import com.greenlink.core.application.vendor.usecase.UploadVendorDocumentUseCase;
import com.greenlink.core.application.vendor.usecase.VendorSignupUseCase;
import com.greenlink.core.application.vendor.usecase.VendorSignupWithDocumentsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VendorSignupWithDocumentsService implements VendorSignupWithDocumentsUseCase {

    private static final Logger log = LoggerFactory.getLogger(VendorSignupWithDocumentsService.class);

    private final VendorSignupUseCase vendorSignupUseCase;
    private final UploadVendorDocumentUseCase uploadVendorDocumentUseCase;
    private final SignupDocumentValidator validator;

    public VendorSignupWithDocumentsService(VendorSignupUseCase vendorSignupUseCase,
                                            UploadVendorDocumentUseCase uploadVendorDocumentUseCase,
                                            SignupDocumentValidator validator) {
        this.vendorSignupUseCase = vendorSignupUseCase;
        this.uploadVendorDocumentUseCase = uploadVendorDocumentUseCase;
        this.validator = validator;
    }

    @Override
    public VendorSignupWithDocumentsResult signup(VendorSignupCommand cmd,
                                                  SignupDocument businessReg, SignupDocument bankBook) {
        SignupDocument validBusinessReg = validator.validate(businessReg);
        SignupDocument validBankBook = validator.validate(bankBook);

        VendorResult vendor = vendorSignupUseCase.signup(cmd);

        List<String> failed = new ArrayList<>();
        upload(vendor, "businessReg", vendor.businessRegUrl(), validBusinessReg, failed);
        upload(vendor, "bankBook", vendor.bankBookUrl(), validBankBook, failed);
        return new VendorSignupWithDocumentsResult(vendor, failed);
    }

    private void upload(VendorResult vendor, String docType, String existingUrl,
                        SignupDocument doc, List<String> failed) {
        if (existingUrl != null) return;
        try {
            uploadVendorDocumentUseCase.uploadDocument(
                    vendor.id(), docType, doc.fileName(), doc.content(), doc.contentType());
        } catch (RuntimeException e) {
            log.error("가입 서류 업로드 실패 vendorId={} docType={}", vendor.id(), docType, e);
            failed.add(docType);
        }
    }
}
