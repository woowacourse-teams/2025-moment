CREATE TABLE IF NOT EXISTS reports
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    report_reason VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_reports_users
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    );

-- 인덱스 추가 쿼리 (변경 없음)
CREATE INDEX idx_reports_on_target ON reports (target_type, target_id);

-- 유니크 제약조건 추가 쿼리 (H2 표준 문법으로 변경)
ALTER TABLE reports ADD CONSTRAINT uidx_reports_on_user_target UNIQUE (user_id, target_type, target_id);
