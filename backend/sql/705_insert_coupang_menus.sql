-- [#705] 쿠팡 메뉴 그룹 등록
-- 실행 환경: 개발/운영 DB 직접 실행
-- 구조: 쿠팡(/coupang) > 쿠팡 주문 조회(/coupang/orders), 쿠팡 연동 설정(/coupang/settings)
-- 권한: SUPER_ADMIN, ORDERING

INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
SELECT '쿠팡', '/coupang', 75, NULL, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE url = '/coupang');

INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
SELECT '쿠팡 주문 조회', '/coupang/orders', 1, m.id, NOW(), NOW()
FROM menus m WHERE m.url = '/coupang'
  AND NOT EXISTS (SELECT 1 FROM menus WHERE url = '/coupang/orders');

INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
SELECT '쿠팡 연동 설정', '/coupang/settings', 2, m.id, NOW(), NOW()
FROM menus m WHERE m.url = '/coupang'
  AND NOT EXISTS (SELECT 1 FROM menus WHERE url = '/coupang/settings');

INSERT INTO menu_roles (menu_id, role)
SELECT m.id, r.role
FROM menus m
JOIN (SELECT 'SUPER_ADMIN' AS role UNION ALL SELECT 'ORDERING') r
WHERE m.url IN ('/coupang', '/coupang/orders', '/coupang/settings')
  AND NOT EXISTS (
    SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = r.role
  );
