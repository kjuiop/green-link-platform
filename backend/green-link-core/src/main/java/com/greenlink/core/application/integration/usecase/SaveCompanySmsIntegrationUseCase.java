package com.greenlink.core.application.integration.usecase;

import com.greenlink.core.application.integration.dto.CompanySmsIntegrationResult;
import com.greenlink.core.application.integration.dto.SaveCompanySmsIntegrationCommand;

public interface SaveCompanySmsIntegrationUseCase {
    CompanySmsIntegrationResult save(SaveCompanySmsIntegrationCommand command);
}
