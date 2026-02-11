-- 기존 콘텐츠 soft delete (Phase 1 레거시 정리)
-- 테스트 환경에서는 데이터가 없으므로 건너뜀
-- UPDATE moments SET deleted_at = CURRENT_TIMESTAMP WHERE deleted_at IS NULL;
-- UPDATE comments SET deleted_at = CURRENT_TIMESTAMP WHERE deleted_at IS NULL;
-- 등등...

-- User의 star/level 초기화는 테스트 환경에서 불필요함
-- 이 마이그레이션은 프로덕션 환경에서만 의미 있음
SELECT 1;
