package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.smssettings.dto.FaxSettingsResult;
import com.greenlink.core.application.smssettings.dto.SaveFaxSettingsCommand;
import com.greenlink.core.application.smssettings.usecase.FaxSettingsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings/sms/fax")
public class FaxSettingsController {

    private final FaxSettingsUseCase useCase;

    public FaxSettingsController(FaxSettingsUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<FaxSettingsResult> get() {
        return ResponseEntity.ok(useCase.get());
    }

    @PutMapping
    public ResponseEntity<FaxSettingsResult> save(@RequestBody SaveFaxSettingsCommand command) {
        return ResponseEntity.ok(useCase.save(command));
    }
}
