package com.greenlink.core.application.sms.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.sms.dto.SmsMessageResult;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GetSmsMessagesUseCase {
    List<SmsMessageResult> getMessages();
    List<SmsMessageResult> getMessagesByOrder(Long orderId);
    PageResult<SmsMessageResult> getMessagesPaged(Pageable pageable);
}
