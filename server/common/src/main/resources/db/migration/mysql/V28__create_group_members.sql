CREATE TABLE group_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    nickname VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_members_group FOREIGN KEY (group_id) REFERENCES `groups`(id),
    CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_member UNIQUE (group_id, user_id)
);

CREATE INDEX idx_members_group ON group_members(group_id);
CREATE INDEX idx_members_user ON group_members(user_id);
CREATE INDEX idx_members_status ON group_members(status);
CREATE INDEX idx_members_group_nickname ON group_members(group_id, nickname);
