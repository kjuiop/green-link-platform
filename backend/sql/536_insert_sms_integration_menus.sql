-- [#536] 문자 연동관리 메뉴 그룹 추가 + 문자 발송내역 이동
-- 실행 환경: 개발/운영 DB 직접 실행

-- 1. 문자 연동관리 부모 메뉴 추가
INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
SELECT '문자 연동관리', '/sms', 70, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE url = '/sms');

-- 2. 문자 연동 설정 하위 메뉴
INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
SELECT '문자 연동 설정', '/sms/settings', 1, m.id, NOW(), NOW()
FROM menus m WHERE m.url = '/sms'
  AND NOT EXISTS (SELECT 1 FROM menus WHERE url = '/sms/settings');

-- 3. 문자 발송내역 하위 메뉴 (기존 /settings/message-log 이동)
INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
SELECT '문자 발송내역', '/sms/message-log', 2, m.id, NOW(), NOW()
FROM menus m WHERE m.url = '/sms'
  AND NOT EXISTS (SELECT 1 FROM menus WHERE url = '/sms/message-log');

-- 4. 문자 연동관리 그룹 역할: SUPER_ADMIN, ORDERING
INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'SUPER_ADMIN' FROM menus m WHERE m.url = '/sms'
  AND NOT EXISTS (SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'SUPER_ADMIN');

INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'ORDERING' FROM menus m WHERE m.url = '/sms'
  AND NOT EXISTS (SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'ORDERING');

-- 5. 문자 연동 설정 역할
INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'SUPER_ADMIN' FROM menus m WHERE m.url = '/sms/settings'
  AND NOT EXISTS (SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'SUPER_ADMIN');

INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'ORDERING' FROM menus m WHERE m.url = '/sms/settings'
  AND NOT EXISTS (SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'ORDERING');

-- 6. 문자 발송내역 역할
INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'SUPER_ADMIN' FROM menus m WHERE m.url = '/sms/message-log'
  AND NOT EXISTS (SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'SUPER_ADMIN');

INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'ORDERING' FROM menus m WHERE m.url = '/sms/message-log'
  AND NOT EXISTS (SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'ORDERING');
