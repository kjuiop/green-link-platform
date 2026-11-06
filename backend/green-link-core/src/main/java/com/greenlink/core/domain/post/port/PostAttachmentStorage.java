package com.greenlink.core.domain.post.port;

public interface PostAttachmentStorage {

    record UploadResult(String key, String url) {}

    UploadResult store(Long postId, String originalFileName, byte[] content, String contentType);

    void delete(String s3Key);
}
