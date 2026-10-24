INSERT INTO menus (name, url, sort_order, parent_id, created_at, updated_at)
VALUES ('상품관리', '/products', 5, NULL, NOW(), NOW());

INSERT INTO menu_roles (menu_id, role)
SELECT id, 'SUPER_ADMIN' FROM menus WHERE url = '/products';

INSERT INTO menu_roles (menu_id, role)
SELECT id, 'ORDERING' FROM menus WHERE url = '/products';
