ALTER TABLE sms_settings
  ADD COLUMN provider   VARCHAR(50) NOT NULL DEFAULT 'ALIGO'  AFTER template_others,
  ADD COLUMN api_key    VARCHAR(200)          DEFAULT NULL     AFTER provider,
  ADD COLUMN user_id    VARCHAR(100)          DEFAULT NULL     AFTER api_key;
