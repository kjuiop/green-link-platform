package com.greenlink.core.application.smssettings.usecase;

import com.greenlink.core.application.smssettings.dto.FaxSettingsResult;
import com.greenlink.core.application.smssettings.dto.SaveFaxSettingsCommand;

public interface FaxSettingsUseCase {
    FaxSettingsResult get();

    FaxSettingsResult save(SaveFaxSettingsCommand command);
}
