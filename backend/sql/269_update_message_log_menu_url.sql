-- [#269] 문자 발송내역 메뉴 URL 변경: /messages → /settings/message-log
-- 실행 환경: 개발/운영 DB 직접 실행

-- 1. 기존 /messages URL을 /settings/message-log 로 변경 (있는 경우)
UPDATE menus SET url = '/settings/message-log' WHERE url = '/messages';

-- 2. /settings/message-log 메뉴가 없으면 신규 삽입
INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
SELECT '문자 발송내역', '/settings/message-log', 1, m.id, NOW(), NOW()
FROM menus m
WHERE m.url = '/settings'
  AND NOT EXISTS (SELECT 1 FROM menus WHERE url = '/settings/message-log');

-- 3. 역할 추가 (없는 경우에만)
INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'SUPER_ADMIN'
FROM menus m
WHERE m.url = '/settings/message-log'
  AND NOT EXISTS (
    SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'SUPER_ADMIN'
  );

INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'ORDERING'
FROM menus m
WHERE m.url = '/settings/message-log'
  AND NOT EXISTS (
    SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'ORDERING'
  );
