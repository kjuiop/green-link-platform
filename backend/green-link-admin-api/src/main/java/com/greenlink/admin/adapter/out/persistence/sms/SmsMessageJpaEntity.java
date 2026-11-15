package com.greenlink.admin.adapter.out.persistence.sms;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.sms.model.MessageChannel;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.model.SmsMessageType;
import com.greenlink.core.domain.sms.model.SmsStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "message_logs")
public class SmsMessageJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, length = 20)
    private String receiver;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SmsStatus status;

    @Column(columnDefinition = "TEXT")
    private String failReason;

    @Column(length = 50)
    private String aligoMsgId;

    @Column(length = 30)
    private String sentAt;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SmsMessageType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageChannel channel = MessageChannel.SMS;

    @Column(length = 100)
    private String templateCode;

    @Column(nullable = false)
    private boolean isReplaced = false;

    protected SmsMessageJpaEntity() {}

    public static SmsMessageJpaEntity from(SmsMessage sms) {
        SmsMessageJpaEntity e = new SmsMessageJpaEntity();
        e.id = sms.getId();
        e.orderId = sms.getOrderId();
        e.receiver = sms.getReceiver();
        e.message = sms.getMessage();
        e.status = sms.getStatus();
        e.failReason = sms.getFailReason();
        e.aligoMsgId = sms.getAligoMsgId();
        e.sentAt = sms.getSentAt();
        e.imageUrl = sms.getImageUrl();
        e.type = sms.getType() != null ? sms.getType() : SmsMessageType.DELIVERY;
        e.channel = sms.getChannel() != null ? sms.getChannel() : MessageChannel.SMS;
        e.templateCode = sms.getTemplateCode();
        e.isReplaced = sms.isReplaced();
        return e;
    }

    public SmsMessage toDomain() {
        return SmsMessage.of(id, orderId, receiver, message, status, failReason, aligoMsgId, sentAt, imageUrl, type,
                channel, templateCode, isReplaced);
    }
}
