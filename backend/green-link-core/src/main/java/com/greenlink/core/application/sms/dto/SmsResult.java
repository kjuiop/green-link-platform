package com.greenlink.core.application.sms.dto;

public record SmsResult(boolean success, String msgId, String failReason) {}
