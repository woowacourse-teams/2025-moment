ALTER TABLE moments ADD COLUMN group_id BIGINT DEFAULT NULL;
ALTER TABLE moments ADD COLUMN member_id BIGINT DEFAULT NULL;

ALTER TABLE moments ADD CONSTRAINT fk_moments_group
    FOREIGN KEY (group_id) REFERENCES groups(id);
ALTER TABLE moments ADD CONSTRAINT fk_moments_member
    FOREIGN KEY (member_id) REFERENCES group_members(id);

CREATE INDEX idx_moments_group ON moments(group_id);
CREATE INDEX idx_moments_member ON moments(member_id);
