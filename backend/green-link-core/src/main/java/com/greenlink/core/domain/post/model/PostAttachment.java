package com.greenlink.core.domain.post.model;

public class PostAttachment {

    private Long id;
    private Long postId;
    private String fileName;
    private String s3Key;
    private String url;

    private PostAttachment() {}

    public static PostAttachment of(Long postId, String fileName, String s3Key, String url) {
        PostAttachment attachment = new PostAttachment();
        attachment.postId = postId;
        attachment.fileName = fileName;
        attachment.s3Key = s3Key;
        attachment.url = url;
        return attachment;
    }

    public static PostAttachment ofWithId(Long id, Long postId, String fileName, String s3Key, String url) {
        PostAttachment attachment = new PostAttachment();
        attachment.id = id;
        attachment.postId = postId;
        attachment.fileName = fileName;
        attachment.s3Key = s3Key;
        attachment.url = url;
        return attachment;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public String getFileName() { return fileName; }
    public String getS3Key() { return s3Key; }
    public String getUrl() { return url; }
}
