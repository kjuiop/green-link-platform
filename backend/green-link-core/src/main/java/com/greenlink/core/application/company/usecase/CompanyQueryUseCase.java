package com.greenlink.core.application.company.usecase;

import com.greenlink.core.application.company.dto.CompanyInfo;

import java.util.Optional;

public interface CompanyQueryUseCase {
    Optional<CompanyInfo> findByBusinessNo(String businessNo);
}
