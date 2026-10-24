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

    public void initForOrder(Long orderId) {
        this.orderId = orderId;
        this.delivered = false;
        this.confirmedAt = LocalDateTime.now();
    }

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

    public void update(String receiverName, String receiverRelation,
                       boolean delivered, String memo, LocalDateTime confirmedAt) {
        this.receiverName = receiverName;
        this.receiverRelation = receiverRelation;
        this.delivered = delivered;
        this.memo = memo;
        this.confirmedAt = confirmedAt;
    }

    public void clearPhotos() {
        photos.clear();
    }

    public void addPhoto(String photoUrl, String s3Key, String photoType) {
        ReceiptPhotoJpaEntity photo = new ReceiptPhotoJpaEntity(this, photoUrl, s3Key, photoType);
        photos.add(photo);
    }

    public ReceiptConfirmation toDomain() {
        List<String> sitePhotoUrls = photos.stream()
                .filter(p -> "SITE".equals(p.getPhotoType()))
                .map(ReceiptPhotoJpaEntity::getPhotoUrl)
                .toList();
        List<String> storePhotoUrls = photos.stream()
                .filter(p -> "STORE".equals(p.getPhotoType()))
                .map(ReceiptPhotoJpaEntity::getPhotoUrl)
                .toList();
        return ReceiptConfirmation.of(id, orderId, receiverName, receiverRelation,
                delivered, memo, confirmedAt, sitePhotoUrls, storePhotoUrls);
    }

    public Long getId() { return id; }
    public boolean isDelivered() { return delivered; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
}
