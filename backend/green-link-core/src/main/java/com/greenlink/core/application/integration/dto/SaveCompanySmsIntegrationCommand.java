package com.greenlink.core.application.integration.dto;

public record SaveCompanySmsIntegrationCommand(
        Long companyId,
        String provider,
        String apiKey,
        String senderNumber
) {}
