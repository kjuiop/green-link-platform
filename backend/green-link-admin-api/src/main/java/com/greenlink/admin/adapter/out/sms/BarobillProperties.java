package com.greenlink.admin.adapter.out.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "barobill")
public record BarobillProperties(String url) {

    private static final String DEFAULT_URL = "https://testws.baroservice.com/SMS.asmx";

    public BarobillProperties {
        if (url == null || url.isBlank()) {
            url = DEFAULT_URL;
        }
    }

    public String kakaotalkUrl() {
        return url.replaceFirst("(?i)SMS\\.asmx", "KAKAOTALK.asmx");
    }
}
