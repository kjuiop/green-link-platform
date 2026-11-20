-- #713 발주사 연동 사업자번호는 companies.business_no 를 사용하므로 company_integrations.corp_num 컬럼 제거
-- 배포 후 실행한다. (되돌릴 수 없음)

ALTER TABLE company_integrations DROP COLUMN corp_num;
