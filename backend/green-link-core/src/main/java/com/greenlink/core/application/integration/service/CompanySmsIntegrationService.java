package com.greenlink.core.application.integration.service;

import com.greenlink.core.application.common.BusinessNumber;
import com.greenlink.core.application.common.SecretMask;
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
        String corpNum = BusinessNumber.normalize(command.corpNum());
        CompanyIntegration integration = repository.findByCompanyId(command.companyId())
                .orElseGet(() -> CompanyIntegration.create(command.companyId(), command.provider(), command.apiKey(),
                        command.senderNumber(), command.userId(), corpNum));
        String apiKey = SecretMask.resolveKey(command.apiKey(), integration.getApiKey(), integration.getProvider(), command.provider());
        integration.update(command.provider(), apiKey, command.senderNumber(), command.userId(), corpNum);
        integration.updateAlimtalk(command.alimtalkEnabled(), command.alimtalkTemplateFuneral(),
                command.alimtalkTemplateWedding(), command.alimtalkTemplateEtc(), command.alimtalkSenderKey());
        return toResult(repository.save(integration));
    }

    private CompanySmsIntegrationResult toResult(CompanyIntegration ci) {
        return new CompanySmsIntegrationResult(
                ci.getId(), ci.getCompanyId(), ci.getProvider(),
                "BAROBILL".equalsIgnoreCase(ci.getProvider()) ? SecretMask.mask(ci.getApiKey()) : ci.getApiKey(),
                ci.getSenderNumber(), ci.getUserId(), ci.getCorpNum(),
                ci.isAlimtalkEnabled(), ci.getAlimtalkTemplateFuneral(),
                ci.getAlimtalkTemplateWedding(), ci.getAlimtalkTemplateEtc(), ci.getAlimtalkSenderKey()
        );
    }
}
