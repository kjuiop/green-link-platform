-- [#699] 상품 종류(category) 추가
-- 실행 환경: 개발/운영 DB 직접 실행
-- ALTER 는 1회만 실행 (dev/prod 는 ddl-auto=update 라 백엔드가 먼저 떠서 컬럼이 이미 있으면 해당 줄은 건너뛸 것).
-- UPDATE 는 재실행해도 안전 (category IS NULL 인 행만 대상).

ALTER TABLE base_products ADD COLUMN category VARCHAR(20) NULL;
ALTER TABLE products      ADD COLUMN category VARCHAR(20) NULL;

-- 기존 기본상품의 종류를 상품명 키워드로 채움 (먼저 걸리는 규칙이 우선)
UPDATE base_products SET category = 'MEMORIAL'    WHERE category IS NULL AND name LIKE '%근조%';
UPDATE base_products SET category = 'ANNIVERSARY' WHERE category IS NULL AND name LIKE '%축하%';
UPDATE base_products SET category = 'ORCHID_EAST' WHERE category IS NULL AND name LIKE '%동양란%';
UPDATE base_products SET category = 'ORCHID_WEST' WHERE category IS NULL AND name LIKE '%서양란%';
UPDATE base_products SET category = 'INDOOR'      WHERE category IS NULL AND name LIKE '%관엽%';
UPDATE base_products SET category = 'RICE_WREATH' WHERE category IS NULL AND name LIKE '%쌀%';
UPDATE base_products SET category = 'FLOWER'      WHERE category IS NULL AND name LIKE '%꽃%';

-- 수주사 상품(기본상품 복사본)에 기본상품 종류 반영
UPDATE products p
JOIN base_products b ON b.id = p.base_product_id
SET p.category = b.category
WHERE p.category IS NULL;

-- 확인: 종류가 비어 있는 기본상품은 기본상품관리 화면에서 직접 지정
-- SELECT id, name FROM base_products WHERE category IS NULL;
