CREATE TABLE IF NOT EXISTS users
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_email
        UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS moments
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    momenter_id BIGINT NOT NULL,
    content VARCHAR(100) NOT NULL,
    is_matched BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_moments_users
        FOREIGN KEY (momenter_id)
            REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS matchings
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    moment_id BIGINT NOT NULL,
    commenter_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
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
    content VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
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
    created_at TIMESTAMP NOT NULL,
    type VARCHAR(255) NOT NULL,
    CONSTRAINT fk_emojis_users
        FOREIGN KEY (user_id)
            REFERENCES users (id),
    CONSTRAINT fk_emojis_comments
        FOREIGN KEY (comment_id)
            REFERENCES comments (id),
    CONSTRAINT uq_emojis_user_comment_type
        UNIQUE (user_id, comment_id, type)
);

ALTER TABLE users ADD COLUMN IF NOT EXISTS current_point INT NOT NULL DEFAULT 0;

ALTER TABLE users ADD COLUMN IF NOT EXISTS provider_type VARCHAR(20) NOT NULL;

ALTER TABLE users DROP CONSTRAINT IF EXISTS uq_email;

ALTER TABLE users DROP CONSTRAINT IF EXISTS uq_email_provider;

ALTER TABLE users ADD CONSTRAINT uq_email_provider UNIQUE (email, provider_type);

-- 포인트 히스토리 테이블 추가
CREATE TABLE IF NOT EXISTS point_history
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    content_id BIGINT NOT NULL,
    CONSTRAINT uq_user_reason_content
    UNIQUE (user_id, reason, content_id),
    CONSTRAINT fk_history_users
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    );

CREATE TABLE IF NOT EXISTS notifications
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(255) NOT NULL,
    target_type VARCHAR(255) NOT NULL,
    target_id BIGINT NOT NULL,
    is_read BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_notifications_users
    FOREIGN KEY (user_id)
    REFERENCES users (id)
);

-- 유저에 레벨 컬럼 추가
ALTER TABLE users ADD COLUMN level VARCHAR(10) NOT NULL;
