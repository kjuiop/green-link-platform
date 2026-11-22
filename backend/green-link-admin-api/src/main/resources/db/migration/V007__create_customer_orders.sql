-- [#739] customer_orders / coupang_order_details 통합 테이블 생성 및 기존 데이터 마이그레이션

-- 1. customer_orders 테이블 생성
CREATE TABLE customer_orders (
    id                      BIGINT        NOT NULL AUTO_INCREMENT,
    company_id              BIGINT        NOT NULL,
    channel                 VARCHAR(20)   NOT NULL,
    status                  VARCHAR(20)   NOT NULL,
    orderer_name            VARCHAR(100)  NULL,
    orderer_phone           VARCHAR(50)   NULL,
    receiver_name           VARCHAR(100)  NULL,
    receiver_phone          VARCHAR(50)   NULL,
    receiver_address        VARCHAR(500)  NULL,
    receiver_address_detail VARCHAR(500)  NULL,
    delivery_message        VARCHAR(1000) NULL,
    total_price             BIGINT        NOT NULL DEFAULT 0,
    ordered_at              DATETIME(6)   NULL,
    approved_by             BIGINT        NULL,
    approved_at             DATETIME(6)   NULL,
    created_at              DATETIME(6)   NOT NULL,
    updated_at              DATETIME(6)   NOT NULL,
    legacy_coupang_order_id BIGINT        NULL,
    PRIMARY KEY (id),
    INDEX idx_customer_orders_company_channel_status (company_id, channel, status, ordered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. coupang_order_details 테이블 생성
CREATE TABLE coupang_order_details (
    id                   BIGINT      NOT NULL AUTO_INCREMENT,
    customer_order_id    BIGINT      NOT NULL,
    coupang_order_id     BIGINT      NOT NULL,
    coupang_shipment_box_id BIGINT   NOT NULL,
    coupang_status       VARCHAR(30) NULL,
    items_json           TEXT        NULL,
    raw_json             MEDIUMTEXT  NULL,
    created_at           DATETIME(6) NOT NULL,
    updated_at           DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_coupang_order_details_order_shipment (coupang_order_id, coupang_shipment_box_id),
    CONSTRAINT fk_coupang_detail_customer_order FOREIGN KEY (customer_order_id) REFERENCES customer_orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. coupang_orders → customer_orders INSERT INTO SELECT
--    status 매핑: PENDING→PENDING, HELD→HELD, APPROVED→APPROVED, CANCELED→CANCELLED
INSERT INTO customer_orders (
    company_id, channel, status,
    orderer_name, orderer_phone,
    receiver_name, receiver_phone,
    receiver_address, receiver_address_detail,
    delivery_message, total_price, ordered_at,
    approved_by, approved_at,
    created_at, updated_at,
    legacy_coupang_order_id
)
SELECT
    company_id,
    'COUPANG',
    CASE status
        WHEN 'CANCELED' THEN 'CANCELLED'
        ELSE status
    END,
    orderer_name, orderer_phone,
    receiver_name, receiver_phone,
    receiver_address, receiver_address_detail,
    delivery_message, total_price, ordered_at,
    approved_by, approved_at,
    created_at, updated_at,
    id
FROM coupang_orders;

-- 4. coupang_order_details INSERT INTO SELECT
INSERT INTO coupang_order_details (
    customer_order_id,
    coupang_order_id, coupang_shipment_box_id,
    coupang_status, items_json, raw_json,
    created_at, updated_at
)
SELECT
    co.id,
    c.order_id, c.shipment_box_id,
    c.coupang_status, c.items_json, c.raw_json,
    c.created_at, c.updated_at
FROM coupang_orders c
JOIN customer_orders co ON co.legacy_coupang_order_id = c.id;

-- 5. orders 테이블에 customer_order_id 컬럼 추가
ALTER TABLE orders
    ADD COLUMN customer_order_id BIGINT NULL,
    ADD INDEX idx_orders_customer_order_id (customer_order_id);

-- 6. coupang_orders.linked_order_id → orders.customer_order_id 역방향 연결 UPDATE
UPDATE orders o
JOIN coupang_orders c ON c.linked_order_id = o.id
JOIN customer_orders co ON co.legacy_coupang_order_id = c.id
SET o.customer_order_id = co.id
WHERE co.id IS NOT NULL;

-- 7. legacy_coupang_order_id 컬럼 DROP (마이그레이션 완료 후 정리)
ALTER TABLE customer_orders DROP COLUMN legacy_coupang_order_id;
