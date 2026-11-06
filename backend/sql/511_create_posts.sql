CREATE TABLE posts (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    author_id       BIGINT       NOT NULL,
    company_name    VARCHAR(100) NOT NULL,
    title           VARCHAR(200) NOT NULL,
    content         TEXT         NOT NULL,
    is_notice       TINYINT(1)   NOT NULL DEFAULT 0,
    is_pinned       TINYINT(1)   NOT NULL DEFAULT 0,
    notice_start_at DATETIME     NULL,
    notice_end_at   DATETIME     NULL,
    view_count      INT          NOT NULL DEFAULT 0,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    deleted_at      DATETIME     NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post_attachments (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    post_id    BIGINT        NOT NULL,
    file_name  VARCHAR(255)  NOT NULL,
    s3_key     VARCHAR(500)  NOT NULL,
    url        VARCHAR(1000) NOT NULL,
    created_at DATETIME(6)   NOT NULL,
    updated_at DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_post_attachments_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_posts_pinned_notice_end ON posts (is_pinned, notice_end_at);
CREATE INDEX idx_posts_created_at ON posts (created_at DESC);
