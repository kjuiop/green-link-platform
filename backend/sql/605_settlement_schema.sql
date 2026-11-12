-- [#605] 정산 스키마 추가
-- 실행 환경: 개발/운영 DB 직접 실행

-- 1. orders에 지급금액 컬럼 추가 (nullable — 기존 주문에는 미입력)
ALTER TABLE orders
    ADD COLUMN vendor_amount BIGINT NULL AFTER amount;

-- 2. companies에 계좌 정보 컬럼 추가
ALTER TABLE companies
    ADD COLUMN bank_name      VARCHAR(50)  NULL AFTER bank_book_url,
    ADD COLUMN account_number VARCHAR(30)  NULL AFTER bank_name,
    ADD COLUMN account_holder VARCHAR(50)  NULL AFTER account_number;

-- 3. 정산완료 상태 테이블
CREATE TABLE settlements (
    id                    BIGINT      NOT NULL AUTO_INCREMENT,
    company_id            BIGINT      NOT NULL,
    type                  VARCHAR(20) NOT NULL COMMENT 'VENDOR or ORDERING',
    from_date             VARCHAR(10) NOT NULL,
    to_date               VARCHAR(10) NOT NULL,
    completed_at          DATETIME    NULL,
    completed_by_admin_id BIGINT      NULL,
    created_at            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_company_type_period (company_id, type, from_date, to_date),
    INDEX idx_company_type (company_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
