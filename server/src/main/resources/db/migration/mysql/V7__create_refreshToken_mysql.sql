CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    value VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    issued_at VARCHAR(255) NOT NULL,
    expired_at VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_refresh_tokens_users
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);