-- [#689] 포인트관리 메뉴 등록
-- 실행 환경: 개발/운영 DB 직접 실행
-- 관리자: /settings 하위 '포인트관리' (SUPER_ADMIN 전용)
-- 회사:   최상위 '포인트관리' (VENDOR, ORDERING)

-- 1. 관리자 메뉴 (/settings/points)
INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
SELECT '포인트관리', '/settings/points', 99, m.id, NOW(), NOW()
FROM menus m
WHERE m.url = '/settings'
  AND NOT EXISTS (SELECT 1 FROM menus WHERE url = '/settings/points');

INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'SUPER_ADMIN'
FROM menus m
WHERE m.url = '/settings/points'
  AND NOT EXISTS (
    SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'SUPER_ADMIN'
  );

-- 2. 회사 메뉴 (/points)
INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
SELECT '포인트관리', '/points', 99, NULL, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE url = '/points');

INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'VENDOR'
FROM menus m
WHERE m.url = '/points'
  AND NOT EXISTS (
    SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'VENDOR'
  );

INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'ORDERING'
FROM menus m
WHERE m.url = '/points'
  AND NOT EXISTS (
    SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'ORDERING'
  );
