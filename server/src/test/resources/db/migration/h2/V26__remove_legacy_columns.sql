-- User 테이블에서 star/level 컬럼 제거 (존재하는 경우에만)
ALTER TABLE users DROP COLUMN IF EXISTS available_star;
ALTER TABLE users DROP COLUMN IF EXISTS exp_star;
ALTER TABLE users DROP COLUMN IF EXISTS level;

-- Moment 테이블에서 write_type, is_matched 제거 (존재하는 경우에만)
ALTER TABLE moments DROP COLUMN IF EXISTS write_type;
ALTER TABLE moments DROP COLUMN IF EXISTS is_matched;
