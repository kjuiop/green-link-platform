package com.greenlink.core.domain.sms.model;

public class SmsMessage {

    private Long id;
    private Long orderId;
    private String receiver;
    private SmsStatus status;
    private String failReason;
    private String aligoMsgId;
    private String sentAt;

    private SmsMessage() {}

    public static SmsMessage create(Long orderId, String receiver) {
        SmsMessage m = new SmsMessage();
        m.orderId = orderId;
        m.receiver = receiver;
        m.status = SmsStatus.FAIL;
        return m;
    }

    public static SmsMessage of(Long id, Long orderId, String receiver,
                                 SmsStatus status, String failReason,
                                 String aligoMsgId, String sentAt) {
        SmsMessage m = new SmsMessage();
        m.id = id;
        m.orderId = orderId;
        m.receiver = receiver;
        m.status = status;
        m.failReason = failReason;
        m.aligoMsgId = aligoMsgId;
        m.sentAt = sentAt;
        return m;
    }

    public void markSuccess(String aligoMsgId, String sentAt) {
        this.status = SmsStatus.SUCCESS;
        this.aligoMsgId = aligoMsgId;
        this.sentAt = sentAt;
    }

    public void markFail(String failReason, String sentAt) {
        this.status = SmsStatus.FAIL;
        this.failReason = failReason;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getReceiver() { return receiver; }
    public SmsStatus getStatus() { return status; }
    public String getFailReason() { return failReason; }
    public String getAligoMsgId() { return aligoMsgId; }
    public String getSentAt() { return sentAt; }
}
