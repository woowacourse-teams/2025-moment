CREATE INDEX idx_notifications_user_read_type ON notifications (user_id, is_read, notification_type);
