package com.greenlink.admin.adapter.out.persistence.post;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.post.model.PostAttachment;
import jakarta.persistence.*;

@Entity
@Table(name = "post_attachments")
public class PostAttachmentJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Column(nullable = false, length = 1000)
    private String url;

    protected PostAttachmentJpaEntity() {}

    public static PostAttachmentJpaEntity from(PostAttachment attachment) {
        PostAttachmentJpaEntity e = new PostAttachmentJpaEntity();
        e.id = attachment.getId();
        e.postId = attachment.getPostId();
        e.fileName = attachment.getFileName();
        e.s3Key = attachment.getS3Key();
        e.url = attachment.getUrl();
        return e;
    }

    public PostAttachment toDomain() {
        return PostAttachment.ofWithId(id, postId, fileName, s3Key, url);
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public String getFileName() { return fileName; }
    public String getS3Key() { return s3Key; }
    public String getUrl() { return url; }
}
