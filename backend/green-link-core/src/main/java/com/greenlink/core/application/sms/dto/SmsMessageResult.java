package com.greenlink.core.application.sms.dto;

public record SmsMessageResult(
        Long id,
        Long orderId,
        String receiver,
        String status,
        String failReason,
        String aligoMsgId,
        String sentAt,
        String imageUrl
) {}
