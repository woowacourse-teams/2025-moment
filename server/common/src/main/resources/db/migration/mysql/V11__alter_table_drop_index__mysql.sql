ALTER TABLE reward_history ADD INDEX idx_user_id (user_id);
ALTER TABLE reward_history DROP INDEX uq_user_reason_content;
