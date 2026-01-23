-- Local test data initialization
-- These users are only for development/testing environments.

INSERT IGNORE INTO users (email, password, nickname, provider_type, available_star, exp_star, level, created_at, deleted_at)
VALUES 
('test1@email.com', 'password123!@#', '일반유저1', 'EMAIL', 10, 0, 'ASTEROID_WHITE', NOW(), NULL),
('test2@email.com', 'password123!@#', '부자유저2', 'EMAIL', 500, 30, 'METEOR_WHITE', NOW(), NULL),
('test3@email.com', 'password123!@#', '고수유저3', 'EMAIL', 100, 800, 'COMET_SKY', NOW(), NULL),
('test4@email.com', 'password123!@#', '구글유저4', 'GOOGLE', 0, 0, 'ASTEROID_WHITE', NOW(), NULL),
('test5@email.com', 'password123!@#', '차단유저5', 'EMAIL', 0, 0, 'ASTEROID_WHITE', NOW(), NOW());
