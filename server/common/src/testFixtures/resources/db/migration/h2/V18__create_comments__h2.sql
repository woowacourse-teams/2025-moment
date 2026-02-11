CREATE INDEX idx_comments_commenter_created_id ON comments (commenter_id, created_at DESC, id DESC);
