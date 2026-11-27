package com.greenlink.core.application.onurimapplication.usecase;

import com.greenlink.core.application.onurimapplication.dto.OnurimApplicationResult;
import com.greenlink.core.application.vendor.dto.SignupDocument;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationInfo;

public interface ApplyOnurimWithDocumentsUseCase {
    OnurimApplicationResult apply(Long adminId, Long companyId, CompanyType desiredType, OnurimApplicationInfo info,
                                  SignupDocument businessReg, SignupDocument bankBook);
}
