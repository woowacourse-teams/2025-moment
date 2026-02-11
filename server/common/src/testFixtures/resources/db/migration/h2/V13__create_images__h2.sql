CREATE TABLE IF NOT EXISTS moment_images
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    moment_id BIGINT NOT NULL,
    url VARCHAR(2083) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_moment_images_moments
    FOREIGN KEY (moment_id)
    REFERENCES users (id)
    );

CREATE TABLE IF NOT EXISTS comment_images
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    url VARCHAR(2083) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_comment_images_comment
    FOREIGN KEY (comment_id)
    REFERENCES users (id)
    );
