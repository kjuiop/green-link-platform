package com.greenlink.core.application.smssettings.dto;

public record SaveSmsSettingsCommand(
        String senderNumber,
        String senderName,
        String templateFuneral,
        String templateWedding,
        String templateOthers
) {}
