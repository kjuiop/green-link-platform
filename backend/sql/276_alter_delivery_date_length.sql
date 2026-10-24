-- deliveryDate 컬럼을 VARCHAR(10)→VARCHAR(16)으로 확장 (배송요구일 시간 포함 YYYY-MM-DD HH:mm 형식)
ALTER TABLE orders
    MODIFY COLUMN delivery_date VARCHAR(16) NOT NULL;
