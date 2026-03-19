-- H2에서는 JSON 타입 대신 TEXT 사용
DELETE FROM notifications;

ALTER TABLE notifications DROP COLUMN target_type;
ALTER TABLE notifications DROP COLUMN target_id;
DROP INDEX IF EXISTS idx_notifications_group;
ALTER TABLE notifications DROP COLUMN group_id;

ALTER TABLE notifications ADD COLUMN source_data TEXT DEFAULT NULL;
ALTER TABLE notifications ADD COLUMN link VARCHAR(512) DEFAULT NULL;
