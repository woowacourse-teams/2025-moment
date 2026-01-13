-- ========================================
-- 관리자 세션 추적 테이블 (애플리케이션 레벨)
-- ========================================
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

-- ========================================
-- Spring Session JDBC 테이블 생성
-- ========================================
-- Spring Session은 HTTP 세션을 데이터베이스에 영속화하여
-- 서버 재시작 후에도 세션을 유지합니다.
-- ========================================

-- 1. SPRING_SESSION 테이블 (세션 메타데이터)
CREATE TABLE SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL COMMENT 'Spring Session 내부 ID (UUID)',
    SESSION_ID CHAR(36) NOT NULL COMMENT 'HTTP 세션 ID (외부 노출)',
    CREATION_TIME BIGINT NOT NULL COMMENT '세션 생성 시간 (밀리초 타임스탬프)',
    LAST_ACCESS_TIME BIGINT NOT NULL COMMENT '마지막 접근 시간 (밀리초 타임스탬프)',
    MAX_INACTIVE_INTERVAL INT NOT NULL COMMENT '최대 비활성 시간 (초)',
    EXPIRY_TIME BIGINT NOT NULL COMMENT '만료 시간 (밀리초 타임스탬프)',
    PRINCIPAL_NAME VARCHAR(100) COMMENT '인증 주체 이름 (예: 관리자 이메일)',

    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Session JDBC - 세션 메타데이터';

-- 인덱스 생성
CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

-- 2. SPRING_SESSION_ATTRIBUTES 테이블 (세션 속성)
CREATE TABLE SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL COMMENT 'SPRING_SESSION.PRIMARY_ID 참조',
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL COMMENT '속성 이름 (예: ADMIN_ID, ADMIN_ROLE)',
    ATTRIBUTE_BYTES BLOB NOT NULL COMMENT '속성 값 (직렬화된 바이트)',

    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID)
        REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Session JDBC - 세션 속성';

-- ========================================
-- 설명
-- ========================================
-- SPRING_SESSION: 세션 메타데이터를 저장
--   - PRIMARY_ID: Spring Session 내부에서 사용하는 ID
--   - SESSION_ID: 브라우저에 전달되는 SESSION 쿠키 값
--   - CREATION_TIME: 세션 생성 시간 (밀리초)
--   - LAST_ACCESS_TIME: 마지막 접근 시간 (밀리초)
--   - MAX_INACTIVE_INTERVAL: 세션 타임아웃 (초)
--   - EXPIRY_TIME: 세션 만료 시간 (밀리초)
--   - PRINCIPAL_NAME: 인증 주체 (옵션)
--
-- SPRING_SESSION_ATTRIBUTES: 세션 속성 저장
--   - SESSION_PRIMARY_ID: SPRING_SESSION.PRIMARY_ID 참조
--   - ATTRIBUTE_NAME: 속성 이름 (ADMIN_ID, ADMIN_ROLE 등)
--   - ATTRIBUTE_BYTES: 직렬화된 속성 값
--
-- 기존 admin_sessions 테이블과의 관계:
--   - SPRING_SESSION: HTTP 세션 영속화 (Spring이 관리)
--   - admin_sessions: 감사/추적 목적 (애플리케이션이 관리)
--   - 둘 다 session_id를 공유하여 연결
-- ========================================
