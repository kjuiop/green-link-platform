package com.greenlink.admin.infrastructure.config;

import com.greenlink.admin.infrastructure.sms.AligoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AligoProperties.class)
public class SmsConfig {}
