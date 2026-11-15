package com.greenlink.core.application.integration.dto;

public record SaveCompanySmsIntegrationCommand(
        Long companyId,
        String provider,
        String apiKey,
        String senderNumber,
        boolean alimtalkEnabled,
        String alimtalkTemplateFuneral,
        String alimtalkTemplateWedding,
        String alimtalkTemplateEtc
) {}
