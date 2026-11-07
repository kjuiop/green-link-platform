package com.greenlink.core.application.integration.service;

import com.greenlink.core.application.integration.dto.CompanySmsIntegrationResult;
import com.greenlink.core.application.integration.dto.SaveCompanySmsIntegrationCommand;
import com.greenlink.core.application.integration.usecase.GetCompanySmsIntegrationUseCase;
import com.greenlink.core.application.integration.usecase.SaveCompanySmsIntegrationUseCase;
import com.greenlink.core.domain.integration.model.CompanyIntegration;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CompanySmsIntegrationService implements GetCompanySmsIntegrationUseCase, SaveCompanySmsIntegrationUseCase {

    private final CompanyIntegrationRepository repository;

    public CompanySmsIntegrationService(CompanyIntegrationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanySmsIntegrationResult> get(Long companyId) {
        return repository.findByCompanyId(companyId).map(this::toResult);
    }

    @Override
    @Transactional
    public CompanySmsIntegrationResult save(SaveCompanySmsIntegrationCommand command) {
        CompanyIntegration integration = repository.findByCompanyId(command.companyId())
                .orElseGet(() -> CompanyIntegration.create(command.companyId(), command.provider(), command.apiKey(), command.senderNumber()));
        integration.update(command.provider(), command.apiKey(), command.senderNumber());
        return toResult(repository.save(integration));
    }

    private CompanySmsIntegrationResult toResult(CompanyIntegration ci) {
        return new CompanySmsIntegrationResult(ci.getId(), ci.getCompanyId(), ci.getProvider(), ci.getApiKey(), ci.getSenderNumber());
    }
}
