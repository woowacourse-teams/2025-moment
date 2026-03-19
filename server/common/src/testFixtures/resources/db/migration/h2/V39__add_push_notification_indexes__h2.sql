CREATE INDEX idx_push_notifications_device_endpoint ON push_notifications (device_endpoint, deleted_at);
CREATE INDEX idx_push_notifications_user_device ON push_notifications (user_id, device_endpoint, deleted_at);
