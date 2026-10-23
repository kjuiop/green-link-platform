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

    public static SmsMessageJpaEntity from(SmsMessage message) {
        SmsMessageJpaEntity e = new SmsMessageJpaEntity();
        e.id = message.getId();
        e.orderId = message.getOrderId();
        e.receiver = message.getReceiver();
        e.status = message.getStatus();
        e.failReason = message.getFailReason();
        e.aligoMsgId = message.getAligoMsgId();
        e.sentAt = message.getSentAt();
        e.imageUrl = message.getImageUrl();
        return e;
    }

    public SmsMessage toDomain() {
        return SmsMessage.of(id, orderId, receiver, status, failReason, aligoMsgId, sentAt, imageUrl);
    }
}
