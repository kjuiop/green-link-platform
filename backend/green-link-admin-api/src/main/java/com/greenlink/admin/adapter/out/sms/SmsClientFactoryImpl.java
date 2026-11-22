package com.greenlink.admin.adapter.out.sms;

import com.greenlink.core.domain.sms.port.SmsClient;
import com.greenlink.core.domain.sms.port.SmsClientFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SmsClientFactoryImpl implements SmsClientFactory {

    private final SmsClient aligoClient;
    private final SmsClient messageMeClient;
    private final SmsClient barobillClient;

    public SmsClientFactoryImpl(@Qualifier("aligo") SmsClient aligoClient,
                                @Qualifier("messageme") SmsClient messageMeClient,
                                @Qualifier("barobill") SmsClient barobillClient) {
        this.aligoClient = aligoClient;
        this.messageMeClient = messageMeClient;
        this.barobillClient = barobillClient;
    }

    @Override
    public SmsClient getByProvider(String provider) {
        if ("MESSAGEME".equalsIgnoreCase(provider)) return messageMeClient;
        if ("BAROBILL".equalsIgnoreCase(provider)) return barobillClient;
        return aligoClient;
    }
}
