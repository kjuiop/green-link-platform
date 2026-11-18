-- [#697] 상품 메뉴 정리
-- 실행 환경: 개발/운영 DB 직접 실행
-- 설정 > '기본상품관리' (/settings/products): SUPER_ADMIN 전용
-- 최상위 '상품 관리' (/products): VENDOR 전용 (발주사는 상품 생성 불가, 슈퍼관리자는 기본상품관리 사용)

-- 1. 설정 하위 기본상품관리
UPDATE menus SET name = '기본상품관리', updated_at = NOW() WHERE url = '/settings/products';

INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
SELECT '기본상품관리', '/settings/products', 98, m.id, NOW(), NOW()
FROM menus m
WHERE m.url = '/settings'
  AND NOT EXISTS (SELECT 1 FROM menus WHERE url = '/settings/products');

DELETE mr FROM menu_roles mr
JOIN menus m ON m.id = mr.menu_id
WHERE m.url = '/settings/products' AND mr.role <> 'SUPER_ADMIN';

INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'SUPER_ADMIN'
FROM menus m
WHERE m.url = '/settings/products'
  AND NOT EXISTS (SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'SUPER_ADMIN');

-- 2. 수주사 상품 관리
UPDATE menus SET name = '상품 관리', updated_at = NOW() WHERE url = '/products';

INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
SELECT '상품 관리', '/products', 90, NULL, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE url = '/products');

DELETE mr FROM menu_roles mr
JOIN menus m ON m.id = mr.menu_id
WHERE m.url = '/products' AND mr.role <> 'VENDOR';

INSERT INTO menu_roles (menu_id, role)
SELECT m.id, 'VENDOR'
FROM menus m
WHERE m.url = '/products'
  AND NOT EXISTS (SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role = 'VENDOR');
