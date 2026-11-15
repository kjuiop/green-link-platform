ALTER TABLE sms_messages RENAME TO message_logs;

ALTER TABLE message_logs
  ADD COLUMN channel       VARCHAR(20) NOT NULL DEFAULT 'SMS' AFTER type,
  ADD COLUMN template_code VARCHAR(100) DEFAULT NULL,
  ADD COLUMN is_replaced   TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE company_integrations
  ADD COLUMN alimtalk_enabled          TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN alimtalk_template_funeral VARCHAR(100) DEFAULT NULL,
  ADD COLUMN alimtalk_template_wedding VARCHAR(100) DEFAULT NULL,
  ADD COLUMN alimtalk_template_etc     VARCHAR(100) DEFAULT NULL;
