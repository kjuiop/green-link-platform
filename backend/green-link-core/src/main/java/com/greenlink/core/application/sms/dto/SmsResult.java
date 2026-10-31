package com.greenlink.core.application.sms.dto;

public record SmsResult(boolean success, String msgId, String failReason, boolean rejectedByProvider) {

    public SmsResult(boolean success, String msgId, String failReason) {
        this(success, msgId, failReason, false);
    }

    public static SmsResult rejected(String failReason) {
        return new SmsResult(false, null, failReason, true);
    }
}
