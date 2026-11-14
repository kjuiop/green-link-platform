-- [#649] 주문 상품(order_items) 분리 + 주문 총 발주금액(total_amount) 백필
-- 실행 환경: 개발/운영 DB 직접 실행
-- 선행 조건: 백엔드 배포 후 실행 (ddl-auto: update 로 order_items 테이블과 orders.total_amount 컬럼이 생성된 상태)
-- 멱등: 여러 번 실행해도 안전 (이미 상품이 있는 주문은 건너뜀)

-- 1. 기존 주문의 단일 상품 정보를 order_items 로 이관
INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, amount, sort_order, created_at, updated_at)
SELECT o.id,
       o.product_id,
       o.product_name,
       CASE WHEN o.quantity > 0 THEN o.amount DIV o.quantity ELSE o.amount END,
       o.quantity,
       o.amount,
       0,
       o.created_at,
       NOW()
FROM orders o
WHERE NOT EXISTS (SELECT 1 FROM order_items i WHERE i.order_id = o.id);

-- 2. 총 발주금액(상품금액 합계 + 추가비용) 백필
UPDATE orders
SET total_amount = amount + COALESCE(extra_cost, 0)
WHERE total_amount IS NULL;
