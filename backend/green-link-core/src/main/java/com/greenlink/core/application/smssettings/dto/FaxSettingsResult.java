package com.greenlink.core.application.smssettings.dto;

public record FaxSettingsResult(
        String faxSenderNumber,
        String faxUrl,
        String ftpHost,
        Integer ftpPort,
        String ftpUsername,
        String ftpPassword,
        String ftpDirectory
) {}
