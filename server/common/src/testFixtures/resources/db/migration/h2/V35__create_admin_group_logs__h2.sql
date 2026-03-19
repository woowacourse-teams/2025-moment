CREATE TABLE admin_group_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    admin_email VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    group_id BIGINT NOT NULL,
    target_id BIGINT,
    description TEXT,
    before_value TEXT,
    after_value TEXT,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE INDEX idx_admin_group_logs_group_id ON admin_group_logs(group_id);
CREATE INDEX idx_admin_group_logs_admin_id ON admin_group_logs(admin_id);
CREATE INDEX idx_admin_group_logs_type ON admin_group_logs(type);
CREATE INDEX idx_admin_group_logs_created_at ON admin_group_logs(created_at);
