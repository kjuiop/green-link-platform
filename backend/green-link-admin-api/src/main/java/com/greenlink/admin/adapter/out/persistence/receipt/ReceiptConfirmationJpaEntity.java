package com.greenlink.admin.adapter.out.persistence.receipt;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.receipt.model.ReceiptConfirmation;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receipt_confirmations")
public class ReceiptConfirmationJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(length = 100)
    private String receiverName;

    @Column(length = 100)
    private String receiverRelation;

    @Column(nullable = false)
    private boolean delivered;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(nullable = false)
    private LocalDateTime confirmedAt;

    @OneToMany(mappedBy = "confirmation", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ReceiptPhotoJpaEntity> photos = new ArrayList<>();

    protected ReceiptConfirmationJpaEntity() {}

    public static ReceiptConfirmationJpaEntity from(ReceiptConfirmation rc) {
        ReceiptConfirmationJpaEntity e = new ReceiptConfirmationJpaEntity();
        e.orderId = rc.getOrderId();
        e.receiverName = rc.getReceiverName();
        e.receiverRelation = rc.getReceiverRelation();
        e.delivered = rc.isDelivered();
        e.memo = rc.getMemo();
        e.confirmedAt = rc.getConfirmedAt();
        return e;
    }

    public void addPhoto(String photoUrl, String s3Key) {
        ReceiptPhotoJpaEntity photo = new ReceiptPhotoJpaEntity(this, photoUrl, s3Key);
        photos.add(photo);
    }

    public ReceiptConfirmation toDomain() {
        List<String> photoUrls = photos.stream()
                .map(ReceiptPhotoJpaEntity::getPhotoUrl)
                .toList();
        return ReceiptConfirmation.of(id, orderId, receiverName, receiverRelation,
                delivered, memo, confirmedAt, photoUrls);
    }

    public Long getId() { return id; }
}
