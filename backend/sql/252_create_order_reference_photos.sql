CREATE TABLE order_reference_photos (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT       NOT NULL,
    s3_key      VARCHAR(500) NOT NULL,
    photo_url   VARCHAR(1000) NOT NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_ref_photo_order FOREIGN KEY (order_id) REFERENCES orders (id)
);
