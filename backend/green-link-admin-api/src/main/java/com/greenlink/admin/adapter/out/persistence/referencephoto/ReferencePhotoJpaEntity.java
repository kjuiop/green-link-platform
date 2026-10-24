package com.greenlink.admin.adapter.out.persistence.referencephoto;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.referencephoto.model.ReferencePhoto;
import jakarta.persistence.*;

@Entity
@Table(name = "order_reference_photos")
public class ReferencePhotoJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, length = 500)
    private String s3Key;

    @Column(nullable = false, length = 1000)
    private String photoUrl;

    protected ReferencePhotoJpaEntity() {}

    public static ReferencePhotoJpaEntity from(ReferencePhoto photo) {
        ReferencePhotoJpaEntity e = new ReferencePhotoJpaEntity();
        e.id = photo.getId();
        e.orderId = photo.getOrderId();
        e.s3Key = photo.getS3Key();
        e.photoUrl = photo.getPhotoUrl();
        return e;
    }

    public ReferencePhoto toDomain() {
        return ReferencePhoto.of(
                id, orderId, s3Key, photoUrl,
                getCreatedAt() != null ? getCreatedAt().toString() : null
        );
    }

    public Long getOrderId() { return orderId; }
}
