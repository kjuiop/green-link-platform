package com.greenlink.core.domain.sms.port;

import com.greenlink.core.domain.sms.model.SmsResult;

public interface SmsClient {
    SmsResult send(String receiver, String msg, String imageUrl, String sender, String rdate, String rtime, String title);

    default SmsResult sendWithCredentials(String apiKey, String senderNumber,
                                          String receiver, String msg, String imageUrl,
                                          String sendReserve, String sendReserveDate) {
        return send(receiver, msg, imageUrl, senderNumber, null, null, null);
    }
}
