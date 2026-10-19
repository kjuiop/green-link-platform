package com.greenlink.admin.infrastructure.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aligo")
public record AligoProperties(
        String key,
        String userid,
        String sender,
        String url
) {}
