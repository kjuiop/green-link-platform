package com.greenlink.core.domain.photo.port;

public interface PhotoStorage {

    UploadResult store(Long orderId, String originalFileName, byte[] content, String contentType);

    void delete(String s3Key);

    record UploadResult(String s3Key, String url) {}
}
