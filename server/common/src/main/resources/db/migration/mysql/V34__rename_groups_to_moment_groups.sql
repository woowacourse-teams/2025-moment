-- MySQL 'groups' 예약어 충돌 해결을 위해 테이블 이름 변경
-- groups -> moment_groups

-- 외래키 제약조건 임시 삭제
ALTER TABLE `group_members` DROP FOREIGN KEY `fk_members_group`;
ALTER TABLE `group_invite_links` DROP FOREIGN KEY `fk_invite_group`;
ALTER TABLE `moments` DROP FOREIGN KEY `fk_moments_group`;

-- 테이블 이름 변경
RENAME TABLE `groups` TO `moment_groups`;

-- 외래키 재생성
ALTER TABLE `group_members`
    ADD CONSTRAINT `fk_members_group`
    FOREIGN KEY (`group_id`) REFERENCES `moment_groups`(`id`);

ALTER TABLE `group_invite_links`
    ADD CONSTRAINT `fk_invite_group`
    FOREIGN KEY (`group_id`) REFERENCES `moment_groups`(`id`);

ALTER TABLE `moments`
    ADD CONSTRAINT `fk_moments_group`
    FOREIGN KEY (`group_id`) REFERENCES `moment_groups`(`id`);
