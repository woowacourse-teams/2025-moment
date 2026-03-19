CREATE TABLE fallback_questions (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
content VARCHAR(200) NOT NULL,

-- Java의 primitive boolean에 맞춰 NOT NULL과 기본값 설정
is_used BOOLEAN NOT NULL DEFAULT FALSE,

-- BaseEntity에서 상속받은 감사 컬럼 (Y2K38 버그 방지용 DATETIME 사용)
created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
deleted_at DATETIME(6) NULL
);

-- 사용하지 않은(is_used = false) 임시 질문을 빠르게 찾기 위한 인덱스
CREATE INDEX idx_fallback_questions_is_used ON fallback_questions (is_used);