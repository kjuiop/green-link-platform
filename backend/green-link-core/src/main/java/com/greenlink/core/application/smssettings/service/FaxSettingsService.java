package com.greenlink.core.application.smssettings.service;

import com.greenlink.core.application.common.SecretMask;
import com.greenlink.core.application.smssettings.dto.FaxSettingsResult;
import com.greenlink.core.application.smssettings.dto.SaveFaxSettingsCommand;
import com.greenlink.core.application.smssettings.usecase.FaxSettingsUseCase;
import com.greenlink.core.domain.smssettings.model.SmsSettings;
import com.greenlink.core.domain.smssettings.port.SmsSettingsRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.fax.FaxErrorCode;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FaxSettingsService implements FaxSettingsUseCase {

    private final SmsSettingsRepository repository;

    public FaxSettingsService(SmsSettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public FaxSettingsResult get() {
        return toResult(repository.get());
    }

    @Override
    @Transactional
    public FaxSettingsResult save(SaveFaxSettingsCommand command) {
        String faxUrl = blankToNull(command.faxUrl());
        validateUrl(faxUrl);
        Integer port = command.ftpPort();
        if (port != null && (port < 1 || port > 65535)) {
            throw new BusinessException(FaxErrorCode.FAX_FTP_PORT_INVALID);
        }
        SmsSettings settings = repository.get();
        String password = SecretMask.isMasked(command.ftpPassword()) ? settings.getFtpPassword() : blankToNull(command.ftpPassword());
        settings.withFax(
                digits(command.faxSenderNumber()),
                faxUrl,
                blankToNull(command.ftpHost()),
                command.ftpPort(),
                blankToNull(command.ftpUsername()),
                password,
                blankToNull(command.ftpDirectory()));
        return toResult(repository.save(settings));
    }

    private FaxSettingsResult toResult(SmsSettings s) {
        return new FaxSettingsResult(s.getFaxSenderNumber(), s.getFaxUrl(), s.getFtpHost(), s.getFtpPort(),
                s.getFtpUsername(), SecretMask.mask(s.getFtpPassword()), s.getFtpDirectory());
    }

    private void validateUrl(String url) {
        if (url == null) return;
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            boolean ok = "https".equalsIgnoreCase(uri.getScheme()) && host != null
                    && (host.equalsIgnoreCase("baroservice.com") || host.toLowerCase().endsWith(".baroservice.com"));
            if (!ok) throw new BusinessException(FaxErrorCode.FAX_URL_INVALID);
        } catch (URISyntaxException e) {
            throw new BusinessException(FaxErrorCode.FAX_URL_INVALID);
        }
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private String digits(String s) {
        String d = s == null ? "" : s.replaceAll("[^0-9]", "");
        return d.isEmpty() ? null : d;
    }
}
