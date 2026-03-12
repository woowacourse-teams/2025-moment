CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(200) NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    group_id BIGINT,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6) NULL
);

CREATE INDEX idx_questions_lookup ON questions (start_date, end_date, question_type, group_id);
