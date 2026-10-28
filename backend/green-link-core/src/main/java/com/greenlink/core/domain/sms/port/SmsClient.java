package com.greenlink.core.domain.sms.port;

import com.greenlink.core.application.sms.dto.SmsResult;

public interface SmsClient {
    SmsResult send(String receiver, String msg, String imageUrl, String sender, String rdate, String rtime, String title);
}
