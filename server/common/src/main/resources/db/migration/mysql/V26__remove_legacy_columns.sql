-- User 테이블에서 star/level 컬럼 제거
ALTER TABLE users DROP COLUMN available_star;
ALTER TABLE users DROP COLUMN exp_star;
ALTER TABLE users DROP COLUMN level;

-- Moment 테이블에서 write_type, is_matched 제거
ALTER TABLE moments DROP COLUMN write_type;
ALTER TABLE moments DROP COLUMN is_matched;
