package com.greenlink.core.application.onurimapplication.usecase;

import com.greenlink.core.application.onurimapplication.dto.OnurimApplicationResult;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationInfo;

import java.util.Optional;

public interface ApplyOnurimUseCase {
    OnurimApplicationResult apply(Long adminId, Long companyId, CompanyType desiredType, OnurimApplicationInfo info);
    Optional<OnurimApplicationResult> getLatest(Long companyId);
}
