package com.greenlink.core.application.sms.usecase;

import com.greenlink.core.application.sms.dto.SmsMessageResult;

import java.util.List;

public interface GetSmsMessagesUseCase {
    List<SmsMessageResult> getMessages();
    List<SmsMessageResult> getMessagesByOrder(Long orderId);
}
