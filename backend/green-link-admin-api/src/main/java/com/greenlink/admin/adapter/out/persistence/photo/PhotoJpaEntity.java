package com.greenlink.admin.adapter.out.persistence.photo;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.photo.model.Photo;
import com.greenlink.core.domain.photo.model.PhotoType;
import jakarta.persistence.*;

@Entity
@Table(name = "photos")
public class PhotoJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, length = 500)
    private String s3Key;

    @Column(nullable = false, length = 1000)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PhotoType photoType;

    @Column(nullable = false)
    private boolean isSendToCustomer;

    protected PhotoJpaEntity() {}

    public static PhotoJpaEntity from(Photo photo) {
        PhotoJpaEntity e = new PhotoJpaEntity();
        e.id = photo.getId();
        e.orderId = photo.getOrderId();
        e.s3Key = photo.getS3Key();
        e.photoUrl = photo.getPhotoUrl();
        e.photoType = photo.getPhotoType();
        e.isSendToCustomer = photo.isSendToCustomer();
        return e;
    }

    public Photo toDomain() {
        return Photo.of(
                id, orderId, s3Key, photoUrl, photoType, isSendToCustomer,
                getCreatedAt() != null ? getCreatedAt().toString() : null
        );
    }
}
