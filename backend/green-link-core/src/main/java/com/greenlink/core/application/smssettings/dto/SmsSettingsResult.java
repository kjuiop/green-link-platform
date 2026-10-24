package com.greenlink.core.application.smssettings.dto;

public record SmsSettingsResult(
        Long id,
        String senderNumber,
        String senderName,
        String templateFuneral,
        String templateWedding,
        String templateOthers
) {}
