CREATE TABLE admin_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL COMMENT '관리자 ID',
    session_id VARCHAR(512) NOT NULL UNIQUE COMMENT 'HTTP 세션 ID',
    login_time DATETIME NOT NULL COMMENT '로그인 시간',
    last_access_time DATETIME NOT NULL COMMENT '마지막 활동 시간',
    ip_address VARCHAR(50) NOT NULL COMMENT '로그인 IP 주소',
    user_agent VARCHAR(512) NOT NULL COMMENT '브라우저 User-Agent',
    logout_time DATETIME COMMENT '로그아웃 시간 (NULL이면 활성 상태)',
    created_at DATETIME NOT NULL COMMENT '생성 시간',
    deleted_at DATETIME COMMENT 'Soft Delete 시간',

    INDEX idx_admin_id (admin_id),
    INDEX idx_session_id (session_id),
    INDEX idx_last_access_time (last_access_time),
    INDEX idx_deleted_at (deleted_at),

    FOREIGN KEY (admin_id) REFERENCES admins(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 세션 추적 테이블';
