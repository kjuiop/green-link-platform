package com.greenlink.core.application.smssettings.usecase;

import com.greenlink.core.application.smssettings.dto.SaveSmsSettingsCommand;
import com.greenlink.core.application.smssettings.dto.SmsSettingsResult;

public interface SaveSmsSettingsUseCase {
    SmsSettingsResult save(SaveSmsSettingsCommand command);
}
