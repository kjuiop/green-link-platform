CREATE TABLE phone_verifications
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone      VARCHAR(20)  NOT NULL,
    code       VARCHAR(6)   NOT NULL,
    expires_at DATETIME     NOT NULL,
    verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_phone_created (phone, created_at DESC)
);
