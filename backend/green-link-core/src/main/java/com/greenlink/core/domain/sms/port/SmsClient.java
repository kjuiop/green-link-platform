package com.greenlink.core.domain.sms.port;

import com.greenlink.core.domain.sms.model.SmsAccount;
import com.greenlink.core.domain.sms.model.SmsResult;

public interface SmsClient {
    SmsResult send(String receiver, String msg, String imageUrl, String sender, String rdate, String rtime, String title);

    default SmsResult sendWithCredentials(String apiKey, String senderNumber,
                                          String receiver, String msg, String imageUrl,
                                          String sendReserve, String sendReserveDate) {
        return send(receiver, msg, imageUrl, senderNumber, null, null, null);
    }

    default SmsResult sendWithAccount(SmsAccount account, String receiver, String msg, String imageUrl,
                           String rdate, String rtime, String title) {
        return send(receiver, msg, imageUrl, account.senderNumber(), rdate, rtime, title);
    }
}
