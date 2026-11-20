package com.greenlink.admin.adapter.out.persistence.fax;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.fax.model.FaxMessage;
import com.greenlink.core.domain.fax.model.FaxStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fax_messages",
        uniqueConstraints = @UniqueConstraint(name = "uk_fax_messages_order_sequence", columnNames = {"order_id", "sequence"}),
        indexes = @Index(name = "idx_fax_messages_status", columnList = "status"))
public class FaxMessageJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @Column(name = "vendor_company_id", nullable = false)
    private Long vendorCompanyId;

    @Column(name = "to_number", nullable = false, length = 20)
    private String toNumber;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FaxStatus status;

    @Column(name = "send_key", length = 100)
    private String sendKey;

    @Column(name = "fail_reason", length = 200)
    private String failReason;

    @Column(nullable = false)
    private boolean manual;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected FaxMessageJpaEntity() {}

    public static FaxMessageJpaEntity from(FaxMessage m) {
        FaxMessageJpaEntity e = new FaxMessageJpaEntity();
        e.id = m.getId();
        e.orderId = m.getOrderId();
        e.sequence = m.getSequence();
        e.vendorCompanyId = m.getVendorCompanyId();
        e.toNumber = m.getToNumber();
        e.contentHash = m.getContentHash();
        e.status = m.getStatus();
        e.sendKey = m.getSendKey();
        e.failReason = m.getFailReason();
        e.manual = m.isManual();
        e.requestedAt = m.getRequestedAt();
        e.completedAt = m.getCompletedAt();
        return e;
    }

    public FaxMessage toDomain() {
        return FaxMessage.of(id, orderId, sequence, vendorCompanyId, toNumber, contentHash, status, sendKey,
                failReason, manual, requestedAt, completedAt);
    }
}
