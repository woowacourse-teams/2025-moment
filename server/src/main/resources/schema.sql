CREATE TABLE IF NOT EXISTS users
(
	id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	email VARCHAR(255) NOT NULL UNIQUE,
	password VARCHAR(255) NOT NULL,
	nick_name VARCHAR(255) NOT NULL UNIQUE,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS moments
(
	id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	momenter_id BIGINT NOT NULL,
	content VARCHAR(255) NOT NULL,
	is_matched BOOLEAN NOT NULL,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_moments_users
        FOREIGN KEY (momenter_id)
        REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS matchings
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    moment_id BIGINT NOT NULL,
    commenter_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_matchings_moments
        FOREIGN KEY (moment_id)
            REFERENCES moments (id),
    CONSTRAINT fk_matchings_users
        FOREIGN KEY (commenter_id)
            REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS comments
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    commenter_id BIGINT NOT NULL,
    moment_id BIGINT NOT NULL,
    content VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_moments
        FOREIGN KEY (moment_id)
            REFERENCES moments (id),
    CONSTRAINT fk_comments_users
        FOREIGN KEY (commenter_id)
            REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS emojis
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    comment_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(255) NOT NULL,
    CONSTRAINT fk_emojis_users
        FOREIGN KEY (user_id)
            REFERENCES users (id),
    CONSTRAINT fk_emojis_comments
        FOREIGN KEY (comment_id)
            REFERENCES comments (id)
);

