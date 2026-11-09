package com.greenlink.core.application.integration.dto;

public record CompanySmsIntegrationResult(
        Long id,
        Long companyId,
        String provider,
        String apiKey,
        String senderNumber
) {}
