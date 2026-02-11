ALTER TABLE notifications ADD COLUMN group_id BIGINT DEFAULT NULL;

CREATE INDEX idx_notifications_group ON notifications(group_id);
