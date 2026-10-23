package com.greenlink.core.domain.sms.port;

import com.greenlink.core.domain.sms.model.SmsMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SmsMessageRepository {
    SmsMessage save(SmsMessage message);
    List<SmsMessage> findAllByOrderId(Long orderId);
    List<SmsMessage> findAll();
    Page<SmsMessage> findAll(Pageable pageable);
}
