CREATE TABLE moment_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    moment_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_moment_likes_moment FOREIGN KEY (moment_id) REFERENCES moments(id),
    CONSTRAINT fk_moment_likes_member FOREIGN KEY (member_id) REFERENCES group_members(id),
    CONSTRAINT uq_moment_like UNIQUE (moment_id, member_id)
);

CREATE TABLE comment_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES comments(id),
    CONSTRAINT fk_comment_likes_member FOREIGN KEY (member_id) REFERENCES group_members(id),
    CONSTRAINT uq_comment_like UNIQUE (comment_id, member_id)
);

CREATE INDEX idx_moment_likes_moment ON moment_likes(moment_id);
CREATE INDEX idx_moment_likes_member ON moment_likes(member_id);
CREATE INDEX idx_comment_likes_comment ON comment_likes(comment_id);
CREATE INDEX idx_comment_likes_member ON comment_likes(member_id);
