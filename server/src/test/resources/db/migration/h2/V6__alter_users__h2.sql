ALTER TABLE users RENAME COLUMN current_point TO available_star;

ALTER TABLE users
    ADD COLUMN exp_star INT NOT NULL DEFAULT 0;

UPDATE users
SET exp_star = available_star;

ALTER TABLE users ALTER COLUMN level VARCHAR(100);
