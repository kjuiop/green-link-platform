package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.smssettings.dto.SaveSmsSettingsCommand;
import com.greenlink.core.application.smssettings.dto.SmsSettingsResult;
import com.greenlink.core.application.smssettings.usecase.GetSmsSettingsUseCase;
import com.greenlink.core.application.smssettings.usecase.SaveSmsSettingsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings/sms")
public class SmsSettingsController {

    private final GetSmsSettingsUseCase getUseCase;
    private final SaveSmsSettingsUseCase saveUseCase;

    public SmsSettingsController(GetSmsSettingsUseCase getUseCase, SaveSmsSettingsUseCase saveUseCase) {
        this.getUseCase = getUseCase;
        this.saveUseCase = saveUseCase;
    }

    @GetMapping
    public ResponseEntity<SmsSettingsResult> get() {
        return ResponseEntity.ok(getUseCase.get());
    }

    @PutMapping
    public ResponseEntity<SmsSettingsResult> save(@RequestBody SaveSmsSettingsCommand command) {
        return ResponseEntity.ok(saveUseCase.save(command));
    }
}
