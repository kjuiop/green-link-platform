-- [#705] 쿠팡 주문 연동 테이블 생성
-- 실행 환경: 개발/운영 DB 직접 실행 (ddl-auto=validate 환경 대비)

CREATE TABLE IF NOT EXISTS coupang_credentials (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    company_id        BIGINT       NOT NULL,
    vendor_code       VARCHAR(50)  NOT NULL,
    access_key_enc    VARCHAR(500) NOT NULL,
    secret_key_enc    VARCHAR(500) NOT NULL,
    key_issued_at     DATE         NOT NULL,
    key_expires_at    DATE         NOT NULL,
    enabled           TINYINT(1)   NOT NULL DEFAULT 1,
    last_collected_at DATETIME(6)  NULL,
    last_error        VARCHAR(500) NULL,
    last_error_at     DATETIME(6)  NULL,
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_coupang_credentials_company_id (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS coupang_orders (
    id                      BIGINT        NOT NULL AUTO_INCREMENT,
    company_id              BIGINT        NOT NULL,
    order_id                BIGINT        NOT NULL,
    shipment_box_id         BIGINT        NOT NULL,
    status                  VARCHAR(20)   NOT NULL,
    coupang_status          VARCHAR(30)   NULL,
    ordered_at              DATETIME(6)   NULL,
    orderer_name            VARCHAR(100)  NULL,
    orderer_phone           VARCHAR(50)   NULL,
    receiver_name           VARCHAR(100)  NULL,
    receiver_phone          VARCHAR(50)   NULL,
    receiver_address        VARCHAR(500)  NULL,
    receiver_address_detail VARCHAR(500)  NULL,
    delivery_message        VARCHAR(1000) NULL,
    items_json              TEXT          NULL,
    total_price             BIGINT        NOT NULL DEFAULT 0,
    raw_json                MEDIUMTEXT    NULL,
    linked_order_id         BIGINT        NULL,
    approved_by             BIGINT        NULL,
    approved_at             DATETIME(6)   NULL,
    created_at              DATETIME(6)   NOT NULL,
    updated_at              DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_coupang_orders_company_shipment (company_id, shipment_box_id),
    INDEX idx_coupang_orders_company_status (company_id, status, ordered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS coupang_sync_logs (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    company_id     BIGINT       NOT NULL,
    trigger_type   VARCHAR(20)  NOT NULL,
    range_from     DATETIME(6)  NULL,
    range_to       DATETIME(6)  NULL,
    fetched_count  INT          NOT NULL DEFAULT 0,
    inserted_count INT          NOT NULL DEFAULT 0,
    success        TINYINT(1)   NOT NULL,
    error_message  VARCHAR(500) NULL,
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_coupang_sync_logs_company_created (company_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
