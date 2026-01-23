-- Local test data initialization
-- These users are only for development/testing environments.

INSERT IGNORE INTO users (email, password, nickname, provider_type, created_at, deleted_at)
VALUES
('test1@email.com', 'password123!@#', '일반유저1', 'EMAIL', NOW(), NULL),
('test2@email.com', 'password123!@#', '부자유저2', 'EMAIL', NOW(), NULL),
('test3@email.com', 'password123!@#', '고수유저3', 'EMAIL', NOW(), NULL),
('test4@email.com', 'password123!@#', '구글유저4', 'GOOGLE', NOW(), NULL),
('test5@email.com', 'password123!@#', '차단유저5', 'EMAIL', NOW(), NOW());
