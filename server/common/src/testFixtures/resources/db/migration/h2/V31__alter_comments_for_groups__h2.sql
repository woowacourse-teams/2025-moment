ALTER TABLE comments ADD COLUMN member_id BIGINT DEFAULT NULL;

ALTER TABLE comments ADD CONSTRAINT fk_comments_member
    FOREIGN KEY (member_id) REFERENCES group_members(id);

CREATE INDEX idx_comments_member ON comments(member_id);
