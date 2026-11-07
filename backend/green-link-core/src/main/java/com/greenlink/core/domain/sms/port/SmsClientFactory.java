package com.greenlink.core.domain.sms.port;

public interface SmsClientFactory {
    SmsClient getByProvider(String provider);
}
