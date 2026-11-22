package com.greenlink.admin.adapter.out.sms;

import com.greenlink.core.domain.sms.port.AlimtalkClient;
import com.greenlink.core.domain.sms.port.AlimtalkClientFactory;
import org.springframework.stereotype.Component;

@Component
public class AlimtalkClientFactoryImpl implements AlimtalkClientFactory {

    private final MsgmeAlimtalkClient msgmeClient;
    private final BarobillAlimtalkClient barobillClient;

    public AlimtalkClientFactoryImpl(MsgmeAlimtalkClient msgmeClient, BarobillAlimtalkClient barobillClient) {
        this.msgmeClient = msgmeClient;
        this.barobillClient = barobillClient;
    }

    @Override
    public AlimtalkClient getByProvider(String provider) {
        if ("BAROBILL".equalsIgnoreCase(provider)) return barobillClient;
        return msgmeClient;
    }
}
