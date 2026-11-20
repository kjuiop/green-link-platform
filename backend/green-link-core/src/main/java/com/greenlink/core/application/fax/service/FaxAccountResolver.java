package com.greenlink.core.application.fax.service;

import com.greenlink.core.domain.fax.port.FaxGateway.FaxAccount;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FaxAccountResolver {

    private final SmsSettingsRepository smsSettingsRepository;

    public FaxAccountResolver(SmsSettingsRepository smsSettingsRepository) {
        this.smsSettingsRepository = smsSettingsRepository;
    }

    public Optional<FaxAccount> resolve() {
        SmsSettings s = smsSettingsRepository.get();
        if (!"BAROBILL".equalsIgnoreCase(s.getProvider())
                || blank(s.getApiKey()) || blank(s.getCorpNum()) || blank(s.getUserId())
                || blank(s.getFaxSenderNumber())
                || blank(s.getFtpHost()) || blank(s.getFtpUsername()) || blank(s.getFtpPassword())) {
            return Optional.empty();
        }
        return Optional.of(new FaxAccount(s.getApiKey(), s.getCorpNum(), s.getUserId(), s.getFaxSenderNumber(),
                s.getFaxUrl(), s.getFtpHost(), s.getFtpPort(), s.getFtpUsername(), s.getFtpPassword(), s.getFtpDirectory()));
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
