package com.greenlink.core.application.onurimapplication.service;

import com.greenlink.core.application.onurimapplication.dto.OnurimApplicationResult;
import com.greenlink.core.application.onurimapplication.dto.OnurimDocumentRequirement;
import com.greenlink.core.application.onurimapplication.usecase.ApplyOnurimUseCase;
import com.greenlink.core.application.onurimapplication.usecase.ApplyOnurimWithDocumentsUseCase;
import com.greenlink.core.application.vendor.dto.SignupDocument;
import com.greenlink.core.application.vendor.service.SignupDocumentValidator;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.DocumentStorage;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationInfo;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.onurimapplication.OnurimApplicationErrorCode;
import org.springframework.stereotype.Service;

@Service
public class ApplyOnurimWithDocumentsService implements ApplyOnurimWithDocumentsUseCase {

    private final ApplyOnurimUseCase applyOnurimUseCase;
    private final SignupDocumentValidator validator;
    private final DocumentStorage documentStorage;

    public ApplyOnurimWithDocumentsService(ApplyOnurimUseCase applyOnurimUseCase,
                                           SignupDocumentValidator validator,
                                           DocumentStorage documentStorage) {
        this.applyOnurimUseCase = applyOnurimUseCase;
        this.validator = validator;
        this.documentStorage = documentStorage;
    }

    @Override
    public OnurimApplicationResult apply(Long adminId, Long companyId, CompanyType desiredType,
                                         OnurimApplicationInfo info,
                                         SignupDocument businessReg, SignupDocument bankBook) {
        OnurimDocumentRequirement need = applyOnurimUseCase.getDocumentRequirement(adminId, companyId);
        requireMatches(need.needsBusinessReg(), businessReg);
        requireMatches(need.needsBankBook(), bankBook);
        SignupDocument validBusinessReg = need.needsBusinessReg() ? validator.validate(businessReg) : null;
        SignupDocument validBankBook = need.needsBankBook() ? validator.validate(bankBook) : null;

        applyOnurimUseCase.ensureApplicable(adminId, companyId);

        String businessRegUrl = store(companyId, "businessReg", validBusinessReg);
        String bankBookUrl = store(companyId, "bankBook", validBankBook);
        return applyOnurimUseCase.apply(adminId, companyId, desiredType, info, businessRegUrl, bankBookUrl);
    }

    private void requireMatches(boolean needed, SignupDocument doc) {
        if (needed && doc == null) {
            throw new BusinessException(OnurimApplicationErrorCode.DOCUMENT_REQUIRED);
        }
        if (!needed && doc != null) {
            throw new BusinessException(OnurimApplicationErrorCode.DOCUMENT_NOT_ALLOWED);
        }
    }

    private String store(Long companyId, String docType, SignupDocument doc) {
        if (doc == null) return null;
        return documentStorage.store(companyId, docType, doc.fileName(), doc.content(), doc.contentType()).url();
    }
}
