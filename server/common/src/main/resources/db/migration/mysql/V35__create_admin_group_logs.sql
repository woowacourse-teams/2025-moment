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
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    INDEX idx_admin_group_logs_group_id (group_id),
    INDEX idx_admin_group_logs_admin_id (admin_id),
    INDEX idx_admin_group_logs_type (type),
    INDEX idx_admin_group_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
