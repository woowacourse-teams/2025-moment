-- 1. 기존의 잘못된 제약조건 삭제
ALTER TABLE moment_images
DROP CONSTRAINT fk_moment_images_moments;

-- 2. 새로운 올바른 제약조건 추가
ALTER TABLE moment_images
    ADD CONSTRAINT fk_moment_images_moments
        FOREIGN KEY (moment_id) REFERENCES moments (id);

-- 1. 기존의 잘못된 제약조건 삭제
ALTER TABLE comment_images
DROP CONSTRAINT fk_comment_images_comment;

-- 2. 새로운 올바른 제약조건 추가
ALTER TABLE comment_images
    ADD CONSTRAINT fk_comment_images_comments -- 새 이름
        FOREIGN KEY (comment_id) REFERENCES comments (id);
