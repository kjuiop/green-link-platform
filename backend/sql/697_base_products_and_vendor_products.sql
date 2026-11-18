-- [#697] 기본상품(base_products) 도입 및 수주사별 상품 구조 개편
-- 실행 환경: 개발/운영 DB 직접 실행 (ddl-auto=validate 환경 — 백엔드 배포 전에 먼저 실행할 것)
-- 1·3·4번은 재실행해도 안전. 2번 ALTER 는 1회만 실행.
-- dev/prod 는 ddl-auto=update 라 백엔드를 먼저 띄우면 컬럼이 이미 생길 수 있음 → 그 경우 2번에서 존재하는 컬럼 항목은 빼고 실행.

-- 1. 기본상품 테이블
CREATE TABLE IF NOT EXISTS base_products (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    price      BIGINT       NOT NULL,
    memo       VARCHAR(500) NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. products 테이블을 수주사별 상품 테이블로 확장
--    company_id NULL = 구 전역 상품(신규 로직에서 사용하지 않음, 삭제하지 않고 보존)
ALTER TABLE products
    ADD COLUMN company_id      BIGINT  NULL,
    ADD COLUMN base_product_id BIGINT  NULL,
    ADD COLUMN enabled         TINYINT(1) NOT NULL DEFAULT 1,
    ADD INDEX idx_products_company (company_id),
    ADD UNIQUE KEY uk_products_company_base (company_id, base_product_id);

-- 3. 기존 전역 상품을 기본상품으로 복사 (id 유지 — 기존 주문의 product_id 와 발주 폼이 계속 동작)
INSERT INTO base_products (id, name, price, memo, sort_order, created_at, updated_at)
SELECT p.id, p.name, p.price, p.memo, p.sort_order, p.created_at, p.updated_at
FROM products p
WHERE p.company_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM base_products b WHERE b.id = p.id);

-- 4. 수주 가능 회사(VENDOR, BOTH)마다 기본상품 복사본 생성 (가격은 복사 시점의 기본가격)
INSERT INTO products (company_id, base_product_id, name, price, memo, sort_order, enabled, created_at, updated_at)
SELECT c.id, b.id, b.name, b.price, b.memo, b.sort_order, 1, NOW(6), NOW(6)
FROM companies c
JOIN base_products b
WHERE c.type IN ('VENDOR', 'BOTH')
  AND c.display_yn = 1
  AND NOT EXISTS (
    SELECT 1 FROM products p WHERE p.company_id = c.id AND p.base_product_id = b.id
  );
