package com.greenlink.core.domain.sms.model;

public class SmsMessage {

    private Long id;
    private Long orderId;
    private String receiver;
    private String message;
    private SmsStatus status;
    private String failReason;
    private String aligoMsgId;
    private String sentAt;
    private String imageUrl;
    private SmsMessageType type;
    private MessageChannel channel = MessageChannel.SMS;
    private String templateCode;
    private boolean isReplaced = false;

    private SmsMessage() {}

    public static SmsMessage create(Long orderId, String receiver, String message, String imageUrl) {
        SmsMessage m = new SmsMessage();
        m.orderId = orderId;
        m.receiver = receiver;
        m.message = message;
        m.status = SmsStatus.FAIL;
        m.imageUrl = imageUrl;
        m.type = SmsMessageType.DELIVERY;
        m.channel = MessageChannel.SMS;
        return m;
    }

    public static SmsMessage createNotification(Long orderId, String receiver, String message) {
        SmsMessage m = new SmsMessage();
        m.orderId = orderId;
        m.receiver = receiver;
        m.message = message;
        m.status = SmsStatus.FAIL;
        m.type = SmsMessageType.ORDER_NOTIFY;
        m.channel = MessageChannel.SMS;
        return m;
    }

    public static SmsMessage createAlimtalk(Long orderId, String receiver, String templateCode) {
        SmsMessage m = new SmsMessage();
        m.orderId = orderId;
        m.receiver = receiver;
        m.channel = MessageChannel.ALIMTALK;
        m.templateCode = templateCode;
        m.type = SmsMessageType.DELIVERY;
        m.status = SmsStatus.FAIL;
        return m;
    }

    public static SmsMessage of(Long id, Long orderId, String receiver,
                                 String message, SmsStatus status, String failReason,
                                 String aligoMsgId, String sentAt, String imageUrl,
                                 SmsMessageType type, MessageChannel channel,
                                 String templateCode, boolean isReplaced) {
        SmsMessage m = new SmsMessage();
        m.id = id;
        m.orderId = orderId;
        m.receiver = receiver;
        m.message = message;
        m.status = status;
        m.failReason = failReason;
        m.aligoMsgId = aligoMsgId;
        m.sentAt = sentAt;
        m.imageUrl = imageUrl;
        m.type = type != null ? type : SmsMessageType.DELIVERY;
        m.channel = channel != null ? channel : MessageChannel.SMS;
        m.templateCode = templateCode;
        m.isReplaced = isReplaced;
        return m;
    }

    public void markSuccess(String aligoMsgId, String sentAt) {
        this.status = SmsStatus.SUCCESS;
        this.aligoMsgId = aligoMsgId;
        this.sentAt = sentAt;
    }

    public void markSuccessWithFallback(String aligoMsgId, String sentAt, String fallbackReason) {
        markSuccess(aligoMsgId, sentAt);
        this.failReason = "예약 거절 → 즉시발송 전환: " + fallbackReason;
    }

    public void markFail(String failReason, String sentAt) {
        this.status = SmsStatus.FAIL;
        this.failReason = failReason;
        this.sentAt = sentAt;
    }

    public void markAlimtalkSuccess(String sentAt, boolean replaced) {
        this.status = SmsStatus.SUCCESS;
        this.sentAt = sentAt;
        this.isReplaced = replaced;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getReceiver() { return receiver; }
    public String getMessage() { return message; }
    public SmsStatus getStatus() { return status; }
    public String getFailReason() { return failReason; }
    public String getAligoMsgId() { return aligoMsgId; }
    public String getSentAt() { return sentAt; }
    public String getImageUrl() { return imageUrl; }
    public SmsMessageType getType() { return type; }
    public MessageChannel getChannel() { return channel; }
    public String getTemplateCode() { return templateCode; }
    public boolean isReplaced() { return isReplaced; }
}
