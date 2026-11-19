-- [#703] 수주사 수수료 컬럼 추가
-- 실행 환경: 개발/운영 DB 직접 실행
-- ddl-auto=update 로 컬럼이 이미 생성된 환경에서는 ALTER가 'Duplicate column name' 오류를 내므로 해당 문장은 건너뛴다.

-- 1. 회사별 수수료 (상품 개수당 정액, 기본 3000원). 기존 행도 3000으로 채워진다.
ALTER TABLE companies ADD COLUMN vendor_fee BIGINT NOT NULL DEFAULT 3000;

-- 2. 주문 수수료 스냅샷 (수수료 × 전체 수량). 기존 주문은 NULL = 수수료 없음으로 취급된다.
ALTER TABLE orders ADD COLUMN vendor_fee BIGINT NULL;

-- 검증
-- SELECT id, company_name, type, vendor_fee FROM companies WHERE type IN ('VENDOR', 'BOTH');
-- SELECT COUNT(*) FROM orders WHERE vendor_fee IS NOT NULL;
