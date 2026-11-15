ALTER TABLE sms_messages ADD COLUMN company_id BIGINT NULL;

UPDATE sms_messages ml
JOIN orders o ON ml.order_id = o.id
SET ml.company_id = o.ordering_company_id
WHERE ml.company_id IS NULL;
