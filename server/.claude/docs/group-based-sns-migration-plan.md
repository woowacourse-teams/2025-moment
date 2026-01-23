# Moment 그룹 기반 SNS 마이그레이션 계획

## 개요
- **목표**: 공개 커뮤니티 SNS → 그룹 기반 비공개 SNS
- **접근**: Clean Slate (기존 데이터 soft delete, 새로 시작)
- **핵심 기능**: 그룹 + 모멘트 + 코멘트 + 좋아요 (모멘트/코멘트 둘 다)

---

## 현재 코드베이스 분석 결과

### 제거 대상 (사이드 이펙트 주의)

| 시스템 | 파일 | 의존하는 코드 |
|--------|------|--------------|
| **Echo** | `Echo.java`, `EchoService.java`, `EchoRepository.java`, `EchoCreateFacadeService.java` | `CommentApplicationService` (6곳), `NotificationEventHandler` |
| **Tag** | `Tag.java`, `MomentTag.java`, `TagService.java`, `MomentTagService.java`, `TagRepository.java`, `MomentTagRepository.java` | `MomentApplicationService` (5곳) |
| **Reward** | `RewardHistory.java`, `Reason.java`, `RewardService.java`, `StarRewardService.java`, `RewardApplicationService.java`, `RewardRepository.java` | `MomentCreateFacadeService`, `CommentCreateFacadeService`, `EchoCreateFacadeService`, `UserService` |
| **Policy** | `OnceADayPolicy.java`, `PointDeductionPolicy.java`, `BasicMomentCreatePolicy.java`, `ExtraMomentCreatePolicy.java` | `MomentApplicationService` |
| **User 필드** | `availableStar`, `expStar`, `level` | `User.java`, `StarRewardService` |

### 유지 대상 (수정 필요)

| 엔티티 | 현재 | 변경 |
|--------|------|------|
| `Moment` | `momenter_id`, `content`, `write_type` | + `group_id`, `member_id` |
| `Comment` | `commenter_id`, `moment_id`, `content` | + `member_id` |
| `User` | 모든 필드 | - `availableStar`, `expStar`, `level` |
| `MomentImage`, `CommentImage` | 그대로 유지 | 변경 없음 |
| `Notification` | `target_type`, `target_id` | + `group_id` |
| `Report` | 그대로 유지 | 변경 없음 |

---

## Phase 1: 레거시 코드 정리 (Day 1-2)

### 1.1 의존성 제거 순서 (역순으로 제거)

**Step 1: Facade 서비스에서 reward 호출 제거**
```
moment/service/facade/MomentCreateFacadeService.java
  - Line 24: rewardApplicationService.rewardForMoment() 제거
  - Line 31: rewardApplicationService.useReward() 제거

comment/service/facade/CommentCreateFacadeService.java
  - Line 34: rewardApplicationService.rewardForComment() 제거

comment/service/facade/EchoCreateFacadeService.java
  - 파일 전체 삭제 (echo 제거)
```

**Step 2: Application 서비스에서 echo/tag 의존성 제거**
```
moment/service/application/MomentApplicationService.java
  - Line 64: tagService.getOrCreate() 제거
  - Line 66: momentTagService.createAll() 제거
  - Line 129: momentTagService.getMomentTagsByMoment() 제거
  - Line 185: momentTagService.getMomentIdsByTags() 제거
  - Line 202: momentTagService.deleteBy() 제거
  - createBasicMoment(), createExtraMoment()의 Policy 검사 제거

comment/service/application/CommentApplicationService.java
  - Line 41, 55, 141: echoService.getEchosOfComments() 제거
  - Line 96: echoService.deleteBy() 제거
  - Line 190: echoService.saveIfNotExisted() 제거
  - Line 196-198: getEchosBy() 메서드 전체 제거
```

**Step 3: Event Handler에서 echo 이벤트 제거**
```
notification/service/eventHandler/NotificationEventHandler.java
  - Line 33-41: handleEchoCreateEvent() 전체 제거
```

**Step 4: UserService에서 reward 의존성 제거**
```
user/service/user/UserService.java
  - Line 89-93: 닉네임 변경 시 포인트 차감 로직 제거
```

**Step 5: User 엔티티에서 star/level 필드 제거**
```
user/domain/User.java
  - availableStar, expStar, level 필드 제거
  - addStarAndUpdateLevel(), canNotUseStars() 메서드 제거
  - Level enum import 제거

user/domain/Level.java
  - 파일 전체 삭제
```

**Step 6: 제거 대상 파일 삭제**
```
# Echo 시스템
rm comment/domain/Echo.java
rm comment/service/comment/EchoService.java
rm comment/service/facade/EchoCreateFacadeService.java
rm comment/infrastructure/EchoRepository.java
rm comment/dto/EchoCreateEvent.java
rm comment/presentation/EchoController.java  # 있다면

# Tag 시스템
rm moment/domain/Tag.java
rm moment/domain/MomentTag.java
rm moment/service/moment/TagService.java
rm moment/service/moment/MomentTagService.java
rm moment/infrastructure/TagRepository.java
rm moment/infrastructure/MomentTagRepository.java

# Reward 시스템
rm reward/  # 전체 디렉토리 삭제

# Policy
rm moment/domain/OnceADayPolicy.java
rm moment/domain/PointDeductionPolicy.java
rm moment/domain/BasicMomentCreatePolicy.java  # 인터페이스 포함
rm moment/domain/ExtraMomentCreatePolicy.java
```

### 1.2 DB 마이그레이션 - 레거시 데이터 정리

```sql
-- V25__soft_delete_legacy_data.sql

-- 기존 콘텐츠 soft delete
UPDATE moments SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE comments SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE echos SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE moment_tags SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE tags SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE moment_images SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE comment_images SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE notifications SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE reward_history SET deleted_at = NOW() WHERE deleted_at IS NULL;

-- User의 star/level 초기화 (컬럼 삭제 전)
UPDATE users SET available_star = 0, exp_star = 0, level = 'ASTEROID_WHITE';
```

```sql
-- V26__remove_legacy_columns.sql

-- User 테이블에서 star/level 컬럼 제거
ALTER TABLE users DROP COLUMN available_star;
ALTER TABLE users DROP COLUMN exp_star;
ALTER TABLE users DROP COLUMN level;

-- Moment 테이블에서 write_type 제거 (더 이상 BASIC/EXTRA 구분 불필요)
ALTER TABLE moments DROP COLUMN write_type;
ALTER TABLE moments DROP COLUMN is_matched;
```

---

## Phase 2: 그룹 인프라 구축 (Day 3-4)

### 2.1 DB 마이그레이션 - 신규 테이블

```sql
-- V27__create_groups.sql

CREATE TABLE groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT fk_groups_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE INDEX idx_groups_owner ON groups(owner_id);
```

```sql
-- V28__create_group_members.sql

CREATE TABLE group_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    nickname VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',  -- OWNER, MEMBER
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, APPROVED, KICKED
    created_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT fk_members_group FOREIGN KEY (group_id) REFERENCES groups(id),
    CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_member UNIQUE (group_id, user_id)
);

CREATE INDEX idx_members_group ON group_members(group_id);
CREATE INDEX idx_members_user ON group_members(user_id);
CREATE INDEX idx_members_group_nickname ON group_members(group_id, nickname);
```

```sql
-- V29__create_group_invite_links.sql

CREATE TABLE group_invite_links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    code VARCHAR(36) NOT NULL UNIQUE,  -- UUID
    expired_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT fk_invite_group FOREIGN KEY (group_id) REFERENCES groups(id)
);

CREATE INDEX idx_invite_code ON group_invite_links(code);
CREATE INDEX idx_invite_group ON group_invite_links(group_id);
```

```sql
-- V30__alter_moments_for_groups.sql

ALTER TABLE moments ADD COLUMN group_id BIGINT;
ALTER TABLE moments ADD COLUMN member_id BIGINT;

ALTER TABLE moments ADD CONSTRAINT fk_moments_group
    FOREIGN KEY (group_id) REFERENCES groups(id);
ALTER TABLE moments ADD CONSTRAINT fk_moments_member
    FOREIGN KEY (member_id) REFERENCES group_members(id);

CREATE INDEX idx_moments_group ON moments(group_id);
CREATE INDEX idx_moments_member ON moments(member_id);
```

```sql
-- V31__alter_comments_for_groups.sql

ALTER TABLE comments ADD COLUMN member_id BIGINT;

ALTER TABLE comments ADD CONSTRAINT fk_comments_member
    FOREIGN KEY (member_id) REFERENCES group_members(id);

CREATE INDEX idx_comments_member ON comments(member_id);
```

```sql
-- V32__create_likes.sql

CREATE TABLE moment_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    moment_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT fk_moment_likes_moment FOREIGN KEY (moment_id) REFERENCES moments(id),
    CONSTRAINT fk_moment_likes_member FOREIGN KEY (member_id) REFERENCES group_members(id),
    CONSTRAINT uq_moment_like UNIQUE (moment_id, member_id)
);

CREATE TABLE comment_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES comments(id),
    CONSTRAINT fk_comment_likes_member FOREIGN KEY (member_id) REFERENCES group_members(id),
    CONSTRAINT uq_comment_like UNIQUE (comment_id, member_id)
);

CREATE INDEX idx_moment_likes_moment ON moment_likes(moment_id);
CREATE INDEX idx_comment_likes_comment ON comment_likes(comment_id);
```

### 2.2 엔티티 구현

```
src/main/java/moment/group/
├── domain/
│   ├── Group.java                 # name, description, ownerId
│   ├── GroupMember.java       # groupId, userId, nickname, role, status
│   ├── GroupInviteLink.java       # groupId, code, expiredAt, isActive
│   ├── MemberRole.java        # enum: OWNER, MEMBER
│   └── MemberStatus.java      # enum: PENDING, APPROVED, KICKED
├── infrastructure/
│   ├── GroupRepository.java
│   ├── GroupMemberRepository.java
│   └── GroupInviteLinkRepository.java
```

```
src/main/java/moment/like/
├── domain/
│   ├── MomentLike.java
│   └── CommentLike.java
├── infrastructure/
│   ├── MomentLikeRepository.java
│   └── CommentLikeRepository.java
```

### 2.3 기존 엔티티 수정

**Moment.java**
```java
// 추가
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "group_id")
private Group group;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "member_id")
private GroupMember member;

// 제거
// private WriteType writeType;
// private boolean isMatched;
```

**Comment.java**
```java
// 추가
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "member_id")
private GroupMember member;
```

---

## Phase 3: 서비스 구현 (Day 5-7)

### 3.1 그룹 서비스

```
src/main/java/moment/group/service/
├── group/
│   ├── GroupService.java              # 그룹 CRUD
│   └── GroupMemberService.java    # 멤버 관리
├── application/
│   └── GroupApplicationService.java   # 그룹 생성 + 멤버 생성 조율
└── invite/
    └── InviteLinkService.java         # 초대 링크 관리
```

**핵심 메서드:**
- `createGroup(userId, name, description)` → Group + Owner Member 생성
- `joinGroup(userId, inviteCode, nickname)` → PENDING 멤버 생성
- `approveMember(groupId, memberId)` → APPROVED로 변경
- `kickMember(groupId, memberId)` → KICKED로 변경 + soft delete
- `leaveGroup(groupId, userId)` → 멤버 soft delete
- `transferOwnership(groupId, newOwnerId)` → 역할 교환
- `createInviteLink(groupId)` → 7일 만료 UUID 생성

### 3.2 모멘트/코멘트 서비스 수정

**MomentService.java 수정:**
- `create(groupId, memberId, content)` - 그룹 컨텍스트 추가
- `getByGroup(groupId, cursor)` - 그룹 피드
- `getMyMoments(groupId, memberId, cursor)` - 나의 모음집

**CommentService.java 수정:**
- `create(momentId, memberId, content)` - 멤버 기반
- `getByMoment(momentId, cursor)` - 모멘트별 코멘트

### 3.3 좋아요 서비스

```
src/main/java/moment/like/service/
└── LikeService.java
    - toggleMomentLike(momentId, memberId)
    - toggleCommentLike(commentId, memberId)
    - getLikeCount(momentId)
    - getLikeCount(commentId)
    - hasLiked(momentId, memberId)
    - hasLiked(commentId, memberId)
```

---

## Phase 4: API 구현 (Day 8-9)

### 4.1 그룹 API

```
POST   /api/v2/groups                              # 그룹 생성
GET    /api/v2/groups                              # 내 그룹 목록
GET    /api/v2/groups/{groupId}                    # 그룹 상세
PATCH  /api/v2/groups/{groupId}                    # 그룹 수정 (owner만)
DELETE /api/v2/groups/{groupId}                    # 그룹 삭제 (owner만)
```

### 4.2 초대/멤버 API

```
POST   /api/v2/groups/{groupId}/invite             # 초대 링크 생성
GET    /api/v2/invite/{code}                       # 초대 정보 조회
POST   /api/v2/groups/join                         # 가입 신청 {inviteCode, nickname}
GET    /api/v2/groups/{groupId}/members            # 멤버 목록
GET    /api/v2/groups/{groupId}/pending            # 대기자 목록 (owner만)
POST   /api/v2/groups/{groupId}/members/{id}/approve  # 승인
POST   /api/v2/groups/{groupId}/members/{id}/reject   # 거절
DELETE /api/v2/groups/{groupId}/members/{id}       # 강퇴 (owner만)
DELETE /api/v2/groups/{groupId}/leave              # 탈퇴
POST   /api/v2/groups/{groupId}/transfer/{memberId}   # 소유권 이전
PATCH  /api/v2/groups/{groupId}/profile            # 내 프로필 수정
```

### 4.3 콘텐츠 API

```
POST   /api/v2/groups/{groupId}/moments            # 모멘트 작성
GET    /api/v2/groups/{groupId}/moments            # 그룹 피드
GET    /api/v2/groups/{groupId}/my-moments         # 나의 모음집
GET    /api/v2/groups/{groupId}/moments/{id}       # 모멘트 상세
DELETE /api/v2/groups/{groupId}/moments/{id}       # 모멘트 삭제

POST   /api/v2/groups/{groupId}/moments/{id}/comments    # 코멘트 작성
GET    /api/v2/groups/{groupId}/moments/{id}/comments    # 코멘트 목록
DELETE /api/v2/groups/{groupId}/comments/{id}            # 코멘트 삭제

POST   /api/v2/groups/{groupId}/moments/{id}/like        # 모멘트 좋아요 토글
POST   /api/v2/groups/{groupId}/comments/{id}/like       # 코멘트 좋아요 토글
```

---

## Phase 5: 알림 & 마무리 (Day 10-11)

### 5.1 알림 시스템 수정

**Notification 엔티티 수정:**
```java
// 추가
private Long groupId;
```

**NotificationType 추가:**
```java
GROUP_JOIN_REQUEST,      // 가입 신청 (owner에게)
GROUP_JOIN_APPROVED,     // 가입 승인됨
GROUP_KICKED,            // 강퇴됨
MOMENT_LIKED,            // 모멘트 좋아요
COMMENT_LIKED,           // 코멘트 좋아요
NEW_COMMENT              // 새 코멘트 (기존 유지)
```

### 5.2 에러 코드 추가 (ErrorCode.java)

```java
// 그룹 관련
GROUP_NOT_FOUND,
GROUP_NAME_REQUIRED,
NOT_GROUP_OWNER,
CANNOT_DELETE_GROUP_WITH_MEMBERS,

// 멤버 관련
MEMBER_NOT_FOUND,
ALREADY_GROUP_MEMBER,
NOT_GROUP_MEMBER,
CANNOT_KICK_OWNER,
NICKNAME_ALREADY_USED,
NICKNAME_REQUIRED,

// 초대 관련
INVITE_LINK_EXPIRED,
INVITE_LINK_INVALID,
```

---

## 모델/스키마 결정 사항

### 1. momenter_id/commenter_id 처리 방침
- **결정**: 유지 + member_id 추가 (이중 참조)
- **이유**: 기존 쿼리/DTO 호환성 유지, 점진적 마이그레이션 지원
- **영향 범위**:
  - `moment/domain/Moment.java`: momenter_id 유지, group_id + member_id 추가
  - `comment/domain/Comment.java`: commenter_id 유지, member_id 추가
  - `comment/infrastructure/CommentRepository.java`: 기존 쿼리 유지

### 2. Soft Delete + UNIQUE 제약 전략

**group_members 테이블**:
- **문제**: `UNIQUE(group_id, user_id)` + soft delete 시 재가입 불가
- **해결**: 서비스 레이어에서 upsert/restore 패턴
```java
// GroupMemberService.joinOrRestore()
Optional<GroupMember> existing = repository.findByGroupIdAndUserIdIncludeDeleted(groupId, userId);
if (existing.isPresent()) {
    existing.get().restore();  // deleted_at = null, status = PENDING
} else {
    repository.save(new GroupMember(groupId, userId, nickname));
}
```

**likes 테이블**:
- **문제**: `UNIQUE(moment_id, member_id)` + soft delete 시 재좋아요 불가
- **해결**: 토글 시 soft delete 복구 (upsert 패턴)
```java
// LikeService.toggleMomentLike()
Optional<MomentLike> existing = repository.findByMomentIdAndMemberIdIncludeDeleted(momentId, memberId);
if (existing.isPresent()) {
    existing.get().toggleDeleted();  // deleted_at 토글
} else {
    repository.save(new MomentLike(momentId, memberId));
}
```

### 3. 그룹 닉네임 중복 검증
- **결정**: 서비스 레이어 검증 (soft delete 고려)
- **구현**:
```java
// GroupMemberService.validateNickname()
if (memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(groupId, nickname)) {
    throw new MomentException(ErrorCode.NICKNAME_ALREADY_USED);
}
```
- **인덱스**: `CREATE INDEX idx_members_group_nickname ON group_members(group_id, nickname);`

---

## 주요 사이드 이펙트 체크리스트

### 컴파일 에러 발생 예상 지점

| 위치 | 원인 | 해결 |
|------|------|------|
| `MomentApplicationService` | TagService 의존성 | 관련 코드 삭제 |
| `MomentApplicationService` | Policy 의존성 | Policy 검사 코드 삭제 |
| `MomentCreateFacadeService` | RewardApplicationService 의존성 | reward 호출 삭제 |
| `CommentApplicationService` | EchoService 의존성 | echo 관련 코드 삭제 |
| `CommentCreateFacadeService` | RewardApplicationService 의존성 | reward 호출 삭제 |
| `NotificationEventHandler` | EchoCreateEvent 의존성 | 핸들러 삭제 |
| `UserService` | Reason enum 의존성 | 닉네임 변경 포인트 차감 삭제 |
| `User` 엔티티 | Level enum 의존성 | star/level 필드 삭제 |

### 추가 영향 범위 (코드 레벨 체크리스트)

#### Reward/Star/Level 제거 영향
| 파일 | 영향 | 해결 |
|------|------|------|
| `user/presentation/MyPageController.java` | star/level 응답 | 필드 제거 |
| `user/dto/response/MyPageProfileResponse.java` | star/level 필드 | 필드 삭제 |
| `admin/service/user/AdminUserService.java` | star/level 조회/수정 | 관련 로직 삭제 |
| `resources/templates/admin/users/list.html` | star/level 표시 | 컬럼 삭제 |

#### Echo 제거 영향
| 파일 | 영향 | 해결 |
|------|------|------|
| `comment/dto/tobe/CommentComposition.java` | echo 디테일 포함 | echo 필드 삭제 |
| `moment/dto/response/MyMomentCommentResponse.java` | echo 정보 포함 | echo 필드 삭제 |

#### Tag 제거 영향
| 파일 | 영향 | 해결 |
|------|------|------|
| `moment/dto/request/MomentCreateRequest.java` | tagNames 필수값 | 필드 삭제 |
| `moment/dto/response/MomentCreateResponse.java` | 태그 목록 응답 | 필드 삭제 |
| `moment/service/facade/CommentableMomentFacadeService.java` | 태그 필터 로직 | 관련 로직 삭제 |

#### WriteType/Policy 제거 영향
| 파일 | 영향 | 해결 |
|------|------|------|
| `moment/domain/WriteType.java` | enum 전체 | 파일 삭제 |
| `moment/infrastructure/MomentRepository.java` | writeType 쿼리 | 쿼리 수정/삭제 |
| `moment/presentation/MomentController.java` | writeType 파라미터 | 파라미터 삭제 |

#### ErrorCode 정리 대상 (`global/exception/ErrorCode.java`)
| 코드 패턴 | 용도 | 처리 |
|-----------|------|------|
| `ECHO_*` | Echo 시스템 | 삭제 |
| `TAG_*` | Tag 시스템 | 삭제 |
| `STAR_*`, `LEVEL_*` | Reward 시스템 | 삭제 |
| `ALREADY_CREATED_TODAY` | OnceADayPolicy | 삭제 |
| `INSUFFICIENT_STAR` | PointDeductionPolicy | 삭제 |

#### Notification 확장 영향
| 파일 | 변경 | 비고 |
|------|------|------|
| `notification/domain/Notification.java` | group_id 필드 추가 | 엔티티 수정 |
| `notification/dto/response/NotificationResponse.java` | group_id 응답 추가 | DTO 수정 |
| `notification/service/eventHandler/NotificationEventHandler.java` | 이벤트에서 group_id 추출 | 핸들러 수정 |
| `notification/service/application/NotificationApplicationService.java` | group_id 파라미터 추가 | 서비스 수정 |

### 테스트 실패 예상 지점

| 테스트 | 원인 | 해결 |
|--------|------|------|
| Echo 관련 테스트 | Echo 시스템 삭제 | 테스트 파일 삭제 |
| Tag 관련 테스트 | Tag 시스템 삭제 | 테스트 파일 삭제 |
| Reward 관련 테스트 | Reward 시스템 삭제 | 테스트 파일 삭제 |
| Moment 생성 테스트 | Policy 삭제 | 테스트 수정 |
| User 레벨 테스트 | Level 삭제 | 테스트 삭제 |

### 마이그레이션 체크리스트 (MySQL + H2 동시 업데이트)

**MySQL** (`src/main/resources/db/migration/mysql/`):
- [ ] V25: 레거시 데이터 soft delete
- [ ] V26: 레거시 컬럼 제거 (star/level/write_type)
- [ ] V27-32: 신규 테이블 및 컬럼 생성

**H2** (`src/test/resources/db/migration/h2/`):
- [ ] MySQL 마이그레이션과 동일한 버전/내용으로 동기화
- [ ] H2 문법 차이 반영 (예: `TIMESTAMP` → `TIMESTAMP NULL`)

**테스트 시드 데이터** (`src/main/resources/sql/test-users.sql`):
- [ ] star/level 컬럼 참조 제거
- [ ] 그룹/멤버 시드 데이터 추가 (E2E 테스트용)

---

## 파일 목록 요약

### 삭제 대상 (21개)
```
# Echo (5개)
comment/domain/Echo.java
comment/service/comment/EchoService.java
comment/service/facade/EchoCreateFacadeService.java
comment/infrastructure/EchoRepository.java
comment/dto/EchoCreateEvent.java

# Tag (6개)
moment/domain/Tag.java
moment/domain/MomentTag.java
moment/service/moment/TagService.java
moment/service/moment/MomentTagService.java
moment/infrastructure/TagRepository.java
moment/infrastructure/MomentTagRepository.java

# Reward (6개)
reward/ 전체 디렉토리

# Policy (3개)
moment/domain/OnceADayPolicy.java
moment/domain/PointDeductionPolicy.java
moment/domain/BasicMomentCreatePolicy.java (또는 인터페이스)

# User Level (1개)
user/domain/Level.java
```

### 신규 생성 (약 25개)
```
group/domain/          (5개: Group, GroupMember, GroupInviteLink, MemberRole, MemberStatus)
group/infrastructure/  (3개: Repository들)
group/service/         (4개: GroupService, GroupMemberService, GroupApplicationService, InviteLinkService)
group/presentation/    (3개: GroupController, GroupMemberController, GroupInviteController)
group/dto/             (약 10개: Request/Response DTOs)

like/domain/           (2개: MomentLike, CommentLike)
like/infrastructure/   (2개: Repository들)
like/service/          (1개: LikeService)
```

### 수정 대상 (12개)
```
moment/domain/Moment.java
moment/service/moment/MomentService.java
moment/service/application/MomentApplicationService.java
moment/service/facade/MomentCreateFacadeService.java

comment/domain/Comment.java
comment/service/comment/CommentService.java
comment/service/application/CommentApplicationService.java
comment/service/facade/CommentCreateFacadeService.java

user/domain/User.java
user/service/user/UserService.java

notification/domain/Notification.java
notification/service/eventHandler/NotificationEventHandler.java
```

---

## 검증 방법

```bash
# 1. 레거시 코드 정리 후
./gradlew compileJava  # 컴파일 에러 확인

# 2. 테스트 정리 후
./gradlew fastTest     # 단위 테스트

# 3. 전체 구현 완료 후
./gradlew build        # 전체 빌드

# 4. E2E 테스트
./gradlew e2eTest
```

### E2E 시나리오
1. 사용자 A 로그인 → 그룹 생성 → 초대 링크 확인
2. 사용자 B 로그인 → 초대 코드로 가입 신청
3. 사용자 A → 가입 승인
4. 사용자 B → 모멘트 작성 → 이미지 업로드
5. 사용자 A → 코멘트 작성 → 좋아요
6. 알림 확인

---

## 리스크 및 롤백 전략

| 리스크 | 대응 |
|--------|------|
| 마이그레이션 실패 | soft delete만 사용, deleted_at을 NULL로 롤백 가능 |
| 코드 변경 중 빌드 실패 | 의존성 제거 순서 엄수 (역순 제거) |
| 테스트 대량 실패 | 관련 테스트 먼저 삭제/수정 |
