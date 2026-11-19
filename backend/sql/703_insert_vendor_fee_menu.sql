-- [#703] 수수료관리 메뉴 등록
-- 실행 환경: 개발/운영 DB 직접 실행
-- 관리자: /settings 하위 '수수료관리' (SUPER_ADMIN 전용)

INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
SELECT '수수료관리', '/settings/vendor-fees', 99, m.id, NOW(), NOW()
FROM menus m
WHERE m.url = '/settings'
  AND NOT EXISTS (SELECT 1 FROM menus WHERE url = '/settings/vendor-fees');

INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'SUPER_ADMIN'
FROM menus m
WHERE m.url = '/settings/vendor-fees'
  AND NOT EXISTS (
    SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'SUPER_ADMIN'
  );
