package com.greenlink.core.domain.referencephoto.model;

public class ReferencePhoto {

    private Long id;
    private Long orderId;
    private String s3Key;
    private String photoUrl;
    private String uploadedAt;

    private ReferencePhoto() {}

    public static ReferencePhoto create(Long orderId, String s3Key, String photoUrl) {
        ReferencePhoto p = new ReferencePhoto();
        p.orderId = orderId;
        p.s3Key = s3Key;
        p.photoUrl = photoUrl;
        return p;
    }

    public static ReferencePhoto of(Long id, Long orderId, String s3Key, String photoUrl, String uploadedAt) {
        ReferencePhoto p = new ReferencePhoto();
        p.id = id;
        p.orderId = orderId;
        p.s3Key = s3Key;
        p.photoUrl = photoUrl;
        p.uploadedAt = uploadedAt;
        return p;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getS3Key() { return s3Key; }
    public String getPhotoUrl() { return photoUrl; }
    public String getUploadedAt() { return uploadedAt; }
}
