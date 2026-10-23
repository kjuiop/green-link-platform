package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.sms.dto.SmsMessageResult;
import com.greenlink.core.application.sms.usecase.GetSmsMessagesUseCase;
import com.greenlink.core.application.sms.usecase.ResendMmsUseCase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SmsController {

    private final GetSmsMessagesUseCase getSmsMessagesUseCase;
    private final ResendMmsUseCase resendMmsUseCase;

    public SmsController(GetSmsMessagesUseCase getSmsMessagesUseCase,
                         ResendMmsUseCase resendMmsUseCase) {
        this.getSmsMessagesUseCase = getSmsMessagesUseCase;
        this.resendMmsUseCase = resendMmsUseCase;
    }

    @GetMapping("/api/messages")
    public ResponseEntity<PageResult<SmsMessageResult>> getMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(getSmsMessagesUseCase.getMessagesPaged(
                PageRequest.of(page, size, Sort.by("id").descending())));
    }

    @GetMapping("/api/messages/order/{orderId}")
    public ResponseEntity<List<SmsMessageResult>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(getSmsMessagesUseCase.getMessagesByOrder(orderId));
    }

    @PostMapping("/api/orders/{orderId}/resend")
    public ResponseEntity<Void> resend(@PathVariable Long orderId) {
        resendMmsUseCase.resend(orderId);
        return ResponseEntity.ok().build();
    }
}
