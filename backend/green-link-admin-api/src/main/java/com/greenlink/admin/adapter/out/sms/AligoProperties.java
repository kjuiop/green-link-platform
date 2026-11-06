package com.greenlink.admin.adapter.out.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aligo")
public record AligoProperties(
        String key,
        String userid,
        String sender,
        String url
) {}
