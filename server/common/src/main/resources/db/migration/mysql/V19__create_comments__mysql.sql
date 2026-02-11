ALTER TABLE comments ADD INDEX idx_commenter_id (commenter_id);
ALTER TABLE comments DROP INDEX idx_comments_commenter_created_id;
