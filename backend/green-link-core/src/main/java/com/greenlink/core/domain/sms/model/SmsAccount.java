package com.greenlink.core.domain.sms.model;

public record SmsAccount(String provider, String apiKey, String senderNumber, String userId, String corpNum) {
}
