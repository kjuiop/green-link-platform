package com.greenlink.core.application.onurimapplication.usecase;

import com.greenlink.core.application.onurimapplication.dto.OnurimApplicationResult;
import com.greenlink.core.application.onurimapplication.dto.OnurimDocumentRequirement;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationInfo;

import java.util.Optional;

public interface ApplyOnurimUseCase {
    OnurimApplicationResult apply(Long adminId, Long companyId, CompanyType desiredType, OnurimApplicationInfo info,
                                  String businessRegUrl, String bankBookUrl);
    OnurimDocumentRequirement getDocumentRequirement(Long adminId, Long companyId);
    void ensureApplicable(Long adminId, Long companyId);
    Optional<OnurimApplicationResult> getLatest(Long companyId);
}
