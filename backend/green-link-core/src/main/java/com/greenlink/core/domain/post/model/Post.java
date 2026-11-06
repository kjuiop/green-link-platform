package com.greenlink.core.domain.post.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Post {

    private Long id;
    private Long authorId;
    private String companyName;
    private String title;
    private String content;
    private boolean isNotice;
    private boolean isPinned;
    private LocalDateTime noticeStartAt;
    private LocalDateTime noticeEndAt;
    private int viewCount;
    private String createdAt;
    private LocalDateTime deletedAt;
    private List<PostAttachment> attachments;

    private Post() {}

    public static Post create(Long authorId, String companyName, String title, String content,
                               boolean isNotice, boolean isPinned,
                               LocalDateTime noticeStartAt, LocalDateTime noticeEndAt) {
        Post post = new Post();
        post.authorId = authorId;
        post.companyName = companyName;
        post.title = title;
        post.content = content;
        post.isNotice = isNotice;
        post.isPinned = isPinned;
        post.noticeStartAt = noticeStartAt;
        post.noticeEndAt = noticeEndAt;
        post.viewCount = 0;
        post.attachments = new ArrayList<>();
        return post;
    }

    public static Post of(Long id, Long authorId, String companyName, String title, String content,
                           boolean isNotice, boolean isPinned,
                           LocalDateTime noticeStartAt, LocalDateTime noticeEndAt,
                           int viewCount, String createdAt, LocalDateTime deletedAt,
                           List<PostAttachment> attachments) {
        Post post = new Post();
        post.id = id;
        post.authorId = authorId;
        post.companyName = companyName;
        post.title = title;
        post.content = content;
        post.isNotice = isNotice;
        post.isPinned = isPinned;
        post.noticeStartAt = noticeStartAt;
        post.noticeEndAt = noticeEndAt;
        post.viewCount = viewCount;
        post.createdAt = createdAt;
        post.deletedAt = deletedAt;
        post.attachments = attachments != null ? attachments : new ArrayList<>();
        return post;
    }

    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void updateNoticeSettings(boolean isNotice, boolean isPinned,
                                      LocalDateTime noticeStartAt, LocalDateTime noticeEndAt) {
        this.isNotice = isNotice;
        this.isPinned = isPinned;
        this.noticeStartAt = noticeStartAt;
        this.noticeEndAt = noticeEndAt;
    }

    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public String getCompanyName() { return companyName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public boolean isNotice() { return isNotice; }
    public boolean isPinned() { return isPinned; }
    public LocalDateTime getNoticeStartAt() { return noticeStartAt; }
    public LocalDateTime getNoticeEndAt() { return noticeEndAt; }
    public int getViewCount() { return viewCount; }
    public String getCreatedAt() { return createdAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public List<PostAttachment> getAttachments() { return attachments; }
}
