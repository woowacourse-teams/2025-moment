-- 1. 기존 알림 데이터 전량 삭제
DELETE FROM notifications;

-- 2. 기존 컬럼 삭제
ALTER TABLE notifications DROP COLUMN target_type;
ALTER TABLE notifications DROP COLUMN target_id;
ALTER TABLE notifications DROP INDEX idx_notifications_group;
ALTER TABLE notifications DROP COLUMN group_id;

-- 3. 신규 컬럼 추가
ALTER TABLE notifications ADD COLUMN source_data JSON DEFAULT NULL;
ALTER TABLE notifications ADD COLUMN link VARCHAR(512) DEFAULT NULL;
