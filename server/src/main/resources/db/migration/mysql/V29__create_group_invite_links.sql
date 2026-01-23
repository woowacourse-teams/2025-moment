CREATE TABLE group_invite_links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    code VARCHAR(36) NOT NULL UNIQUE,
    expired_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_invite_group FOREIGN KEY (group_id) REFERENCES `groups`(id)
);

CREATE INDEX idx_invite_code ON group_invite_links(code);
CREATE INDEX idx_invite_group ON group_invite_links(group_id);
