CREATE TABLE push_notification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_endpoint VARCHAR(255),
    deleted_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_push_notification_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
