-- 발주 이력에 행위자 업체명·담당자명 컬럼 추가
ALTER TABLE order_histories
    ADD COLUMN actor_company VARCHAR(100) NULL AFTER detail,
    ADD COLUMN actor_name    VARCHAR(100) NULL AFTER actor_company;
