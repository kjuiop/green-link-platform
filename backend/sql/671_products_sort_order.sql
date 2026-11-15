-- [#671] products 테이블 sort_order 컬럼 추가
ALTER TABLE products
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0;
