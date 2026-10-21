package com.greenlink.core.domain.photo.model;

public class Photo {

    private Long id;
    private Long orderId;
    private String s3Key;
    private String photoUrl;
    private PhotoType photoType;
    private boolean isSendToCustomer;
    private String uploadedAt;

    private Photo() {}

    public static Photo create(Long orderId, String s3Key, String photoUrl, PhotoType photoType) {
        Photo photo = new Photo();
        photo.orderId = orderId;
        photo.s3Key = s3Key;
        photo.photoUrl = photoUrl;
        photo.photoType = photoType;
        photo.isSendToCustomer = false;
        return photo;
    }

    public static Photo of(Long id, Long orderId, String s3Key, String photoUrl, PhotoType photoType,
                           boolean isSendToCustomer, String uploadedAt) {
        Photo photo = new Photo();
        photo.id = id;
        photo.orderId = orderId;
        photo.s3Key = s3Key;
        photo.photoUrl = photoUrl;
        photo.photoType = photoType;
        photo.isSendToCustomer = isSendToCustomer;
        photo.uploadedAt = uploadedAt;
        return photo;
    }

    public void toggleSendToCustomer(boolean selected) {
        this.isSendToCustomer = selected;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getS3Key() { return s3Key; }
    public String getPhotoUrl() { return photoUrl; }
    public PhotoType getPhotoType() { return photoType; }
    public boolean isSendToCustomer() { return isSendToCustomer; }
    public String getUploadedAt() { return uploadedAt; }
}
