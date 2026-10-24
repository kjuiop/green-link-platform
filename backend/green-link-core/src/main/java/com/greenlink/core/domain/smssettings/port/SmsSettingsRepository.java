package com.greenlink.core.domain.smssettings.port;

import com.greenlink.core.domain.smssettings.model.SmsSettings;

public interface SmsSettingsRepository {
    SmsSettings get();
    SmsSettings save(SmsSettings settings);
}
