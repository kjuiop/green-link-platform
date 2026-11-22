package com.greenlink.core.domain.sms.port;

public interface AlimtalkClientFactory {
    AlimtalkClient getByProvider(String provider);
}
