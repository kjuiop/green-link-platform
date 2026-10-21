package com.greenlink.core.domain.sms.port;

import com.greenlink.core.domain.sms.model.SmsMessage;

import java.util.List;

public interface SmsMessageRepository {
    SmsMessage save(SmsMessage message);
    List<SmsMessage> findAllByOrderId(Long orderId);
    List<SmsMessage> findAll();
}
