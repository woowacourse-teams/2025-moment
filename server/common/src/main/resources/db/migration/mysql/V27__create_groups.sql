CREATE TABLE `groups` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_groups_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE INDEX idx_groups_owner ON `groups`(owner_id);
CREATE INDEX idx_groups_deleted_at ON `groups`(deleted_at);
