package com.greenlink.core.application.integration.usecase;

import com.greenlink.core.application.integration.dto.CompanySmsIntegrationResult;

import java.util.Optional;

public interface GetCompanySmsIntegrationUseCase {
    Optional<CompanySmsIntegrationResult> get(Long companyId);
}
