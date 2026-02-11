CREATE TABLE user_blocks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blocker_id BIGINT NOT NULL,
    blocked_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT uq_user_blocks_blocker_blocked UNIQUE (blocker_id, blocked_user_id),
    CONSTRAINT fk_user_blocks_blocker FOREIGN KEY (blocker_id) REFERENCES users(id),
    CONSTRAINT fk_user_blocks_blocked FOREIGN KEY (blocked_user_id) REFERENCES users(id)
);

CREATE INDEX idx_user_blocks_blocked_user ON user_blocks (blocked_user_id);
