package com.greenlink.core.application.smssettings.service;

import com.greenlink.core.application.common.SecretMask;
import com.greenlink.core.application.smssettings.dto.SaveSmsSettingsCommand;
import com.greenlink.core.application.smssettings.dto.SmsSettingsResult;
import com.greenlink.core.application.smssettings.usecase.GetSmsSettingsUseCase;
import com.greenlink.core.application.smssettings.usecase.SaveSmsSettingsUseCase;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SmsSettingsService implements GetSmsSettingsUseCase, SaveSmsSettingsUseCase {

    private final SmsSettingsRepository repository;

    public SmsSettingsService(SmsSettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public SmsSettingsResult get() {
        return toResult(repository.get());
    }

    @Override
    @Transactional
    public SmsSettingsResult save(SaveSmsSettingsCommand command) {
        SmsSettings settings = repository.get();
        String apiKey = SecretMask.resolveKey(command.apiKey(), settings.getApiKey(), settings.getProvider(), command.provider());
        settings.update(command.senderNumber(), command.senderName(),
                command.templateFuneral(), command.templateWedding(), command.templateOthers(),
                command.provider(), apiKey, command.userId(), command.corpNum());
        return toResult(repository.save(settings));
    }

    private SmsSettingsResult toResult(SmsSettings s) {
        return new SmsSettingsResult(
                s.getId(), s.getSenderNumber(), s.getSenderName(),
                s.getTemplateFuneral(), s.getTemplateWedding(), s.getTemplateOthers(),
                s.getProvider(), "BAROBILL".equalsIgnoreCase(s.getProvider()) ? SecretMask.mask(s.getApiKey()) : s.getApiKey(),
                s.getUserId(), s.getCorpNum());
    }
}
