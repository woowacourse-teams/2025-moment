CREATE TABLE admin_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    session_id VARCHAR(512) NOT NULL UNIQUE,
    login_time TIMESTAMP NOT NULL,
    last_access_time TIMESTAMP NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(512) NOT NULL,
    logout_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_admin_sessions_admin_id FOREIGN KEY (admin_id) REFERENCES admins(id) ON DELETE CASCADE
);

CREATE INDEX idx_admin_sessions_admin_id ON admin_sessions(admin_id);
CREATE INDEX idx_admin_sessions_session_id ON admin_sessions(session_id);
CREATE INDEX idx_admin_sessions_last_access_time ON admin_sessions(last_access_time);
CREATE INDEX idx_admin_sessions_deleted_at ON admin_sessions(deleted_at);
