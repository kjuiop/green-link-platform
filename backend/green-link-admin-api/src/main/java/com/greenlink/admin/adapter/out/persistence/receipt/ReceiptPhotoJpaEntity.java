package com.greenlink.admin.adapter.out.persistence.receipt;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "receipt_photos")
public class ReceiptPhotoJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_confirmation_id", nullable = false)
    private ReceiptConfirmationJpaEntity confirmation;

    @Column(nullable = false, length = 1000)
    private String photoUrl;

    @Column(nullable = false, length = 500)
    private String s3Key;

    @Column(nullable = false, length = 10)
    private String photoType = "SITE";

    protected ReceiptPhotoJpaEntity() {}

    public ReceiptPhotoJpaEntity(ReceiptConfirmationJpaEntity confirmation, String photoUrl, String s3Key, String photoType) {
        this.confirmation = confirmation;
        this.photoUrl = photoUrl;
        this.s3Key = s3Key;
        this.photoType = photoType != null ? photoType : "SITE";
    }

    public String getPhotoUrl() { return photoUrl; }
    public String getS3Key() { return s3Key; }
    public String getPhotoType() { return photoType; }
}
