DROP INDEX idx_comments_commenter_deleted_created_id ON comments;
CREATE INDEX idx_comments_commenter_deleted_created_id_moment ON comments (commenter_id, deleted_at, created_at DESC, id DESC, moment_id);
