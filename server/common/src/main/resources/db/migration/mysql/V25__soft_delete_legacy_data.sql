-- 기존 콘텐츠 soft delete (Phase 1 레거시 정리)
UPDATE moments SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE comments SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE echos SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE moment_tags SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE tags SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE moment_images SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE comment_images SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE notifications SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE reward_history SET deleted_at = NOW() WHERE deleted_at IS NULL;

-- User의 star/level 초기화 (컬럼 삭제 전)
UPDATE users SET available_star = 0, exp_star = 0, level = 'ASTEROID_WHITE' WHERE deleted_at IS NULL;
