package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.sms.dto.SmsMessageResult;
import com.greenlink.core.application.sms.usecase.GetSmsMessagesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
public class SettingsSmsController {

    private final GetSmsMessagesUseCase getSmsMessagesUseCase;

    public SettingsSmsController(GetSmsMessagesUseCase getSmsMessagesUseCase) {
        this.getSmsMessagesUseCase = getSmsMessagesUseCase;
    }

    @GetMapping("/messages")
    public ResponseEntity<List<SmsMessageResult>> getMessages(
            @RequestParam(required = false) Long companyId) {
        if (companyId != null) {
            return ResponseEntity.ok(getSmsMessagesUseCase.getMessagesByCompanyId(companyId));
        }
        return ResponseEntity.ok(getSmsMessagesUseCase.getMessages());
    }
}
