package com.greenlink.admin.adapter.out.persistence.post;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.post.model.Post;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "posts")
public class PostJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_notice", nullable = false)
    private boolean isNotice;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    @Column(name = "notice_start_at")
    private LocalDateTime noticeStartAt;

    @Column(name = "notice_end_at")
    private LocalDateTime noticeEndAt;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected PostJpaEntity() {}

    public static PostJpaEntity from(Post post) {
        PostJpaEntity e = new PostJpaEntity();
        e.id = post.getId();
        e.authorId = post.getAuthorId();
        e.companyName = post.getCompanyName();
        e.title = post.getTitle();
        e.content = post.getContent();
        e.isNotice = post.isNotice();
        e.isPinned = post.isPinned();
        e.noticeStartAt = post.getNoticeStartAt();
        e.noticeEndAt = post.getNoticeEndAt();
        e.viewCount = post.getViewCount();
        e.deletedAt = post.getDeletedAt();
        return e;
    }

    public Post toDomain() {
        return Post.of(
                id,
                authorId,
                companyName,
                title,
                content,
                isNotice,
                isPinned,
                noticeStartAt,
                noticeEndAt,
                viewCount,
                getCreatedAt() != null
                        ? getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : null,
                deletedAt,
                null
        );
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
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
