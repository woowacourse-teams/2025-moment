DROP INDEX idx_comments_commenter_created_id ON comments;
CREATE INDEX idx_comments_commenter_deleted_created_id ON comments (commenter_id, deleted_at, created_at DESC, id DESC);
