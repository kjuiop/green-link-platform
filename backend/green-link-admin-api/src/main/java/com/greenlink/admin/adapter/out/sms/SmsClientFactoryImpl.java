package com.greenlink.admin.adapter.out.sms;

import com.greenlink.core.domain.sms.port.SmsClient;
import com.greenlink.core.domain.sms.port.SmsClientFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SmsClientFactoryImpl implements SmsClientFactory {

    private final SmsClient aligoClient;
    private final SmsClient messageMeClient;

    public SmsClientFactoryImpl(@Qualifier("aligo") SmsClient aligoClient,
                                @Qualifier("messageme") SmsClient messageMeClient) {
        this.aligoClient = aligoClient;
        this.messageMeClient = messageMeClient;
    }

    @Override
    public SmsClient getByProvider(String provider) {
        return "MESSAGEME".equalsIgnoreCase(provider) ? messageMeClient : aligoClient;
    }
}
