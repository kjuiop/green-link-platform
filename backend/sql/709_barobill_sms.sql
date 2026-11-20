-- #709 바로빌 문자 연동: 바로빌 회원사 사업자번호/회원 아이디 컬럼 추가
-- 바로빌 사용 시 매핑: api_key = 연동인증키(CERTKEY), user_id = 바로빌 회원 아이디(SenderID),
--                      corp_num = 바로빌 회원사 사업자번호(하이픈 제외 10자리), sender_number = 사전 등록된 발신번호
-- sms_settings.user_id 는 기존 컬럼을 그대로 사용한다.

ALTER TABLE sms_settings
    ADD COLUMN corp_num VARCHAR(10) NULL COMMENT '바로빌 회원사 사업자번호';

ALTER TABLE company_integrations
    ADD COLUMN user_id VARCHAR(100) NULL COMMENT '바로빌 회원 아이디',
    ADD COLUMN corp_num VARCHAR(10) NULL COMMENT '바로빌 회원사 사업자번호';
