ALTER TABLE orders
    ADD COLUMN product_id BIGINT NULL AFTER ordering_company_id,
    ADD CONSTRAINT fk_orders_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE SET NULL;
