package com.greenlink.admin.adapter.out.persistence.sms;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.model.SmsStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "sms_messages")
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
        return e;
    }

    public SmsMessage toDomain() {
        return SmsMessage.of(id, orderId, receiver, message, status, failReason, aligoMsgId, sentAt, imageUrl);
    }
}
