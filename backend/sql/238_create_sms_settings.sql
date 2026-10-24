-- [#238] 문자 설정 테이블 생성
-- 실행 환경: 개발/운영 DB 직접 실행

CREATE TABLE IF NOT EXISTS sms_settings (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    sender_number VARCHAR(20)  NOT NULL DEFAULT '',
    sender_name   VARCHAR(100) NOT NULL DEFAULT '다운플라워',
    template_funeral TEXT      NOT NULL,
    template_wedding TEXT      NOT NULL,
    template_others  TEXT      NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 초기 기본 데이터 (최초 1회)
INSERT INTO sms_settings (sender_number, sender_name, template_funeral, template_wedding, template_others)
SELECT '', '다운플라워',
    '[다운플라워] {주문자명}님, 삼가 고인의 명복을 빕니다.\n주문하신 근조화환이 {장소}에 배송 완료되었습니다.',
    '[다운플라워] {주문자명}님, 주문하신 축하화환이 {장소}에 배송 완료되었습니다.\n결혼을 진심으로 축하드립니다.',
    '[다운플라워] {주문자명}님, 주문하신 화환이 {장소}에 배송 완료되었습니다.'
WHERE NOT EXISTS (SELECT 1 FROM sms_settings);
