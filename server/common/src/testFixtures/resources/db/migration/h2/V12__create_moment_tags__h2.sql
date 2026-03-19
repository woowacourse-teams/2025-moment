CREATE TABLE IF NOT EXISTS tags
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_tags_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS moment_tags
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    moment_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT moment_tags_moments
        FOREIGN KEY (moment_id)
            REFERENCES moments (id),
    CONSTRAINT moment_tags_tags
            FOREIGN KEY (tag_id)
                REFERENCES tags (id)
);
