# Moment 그룹 기반 SNS 전환 Tech Spec

## 1. 개요

### 1.1 배경
현재 Moment는 모든 사용자가 하나의 공개 커뮤니티에서 모멘트/코멘트를 주고받는 구조입니다. 이 구조는 사용자 수가 충분히 많아야 피드가 활성화되는 문제가 있어, 서비스 활성의 최소 단위를 **전체 사용자 → 그룹**으로 낮추는 전환을 진행합니다.

### 1.2 목표
- 소규모 친밀 기반의 익명 공간(그룹)을 여러 개 만들 수 있게 함
- 유저가 적어도 각자가 자기 그룹 안에서 충분히 사용할 수 있는 구조
- 초대 기반 바이럴 구조로 자연스러운 유입 경로 확보

### 1.3 배포 목표
**2주 내 MVP 배포**

---

## 2. 핵심 변경 사항 요약

| 항목 | 기존 | 변경 |
|------|------|------|
| 콘텐츠 범위 | 전체 공개 커뮤니티 | 그룹 내 한정 |
| 사용자 정체성 | 단일 닉네임 | 그룹별 익명 프로필 |
| 피드 구조 | 전체 피드 | 그룹별 피드 |
| 가입 방식 | 회원가입 후 즉시 사용 | 그룹 초대 → 승인 후 사용 |
| Star/Level 시스템 | 활성 | MVP에서 보류 |
| Echo(반응) 시스템 | 활성 | MVP에서 제거 |
| Tag 시스템 | 활성 | 제거 |
| 코멘트 기간 제한 | 7일 | 제한 없음 |
| API 버전 | v1 | v2 신규 |

### 2.1 레거시 데이터 처리
- 기존 모멘트/코멘트 데이터 **전체 soft delete**
- 깨끗한 상태에서 새 출발

---

## 3. 도메인 모델 설계

### 3.1 신규 엔티티

#### 3.1.1 Group (그룹)
```java
@Entity(name = "groups")
@SQLDelete(sql = "UPDATE groups SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Group extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;                    // 그룹 이름 (필수, 1-30자)

    @Column(nullable = false, length = 200)
    private String description;             // 그룹 설명 (필수, 1-200자)

    @Column(length = 2083)
    private String imageUrl;                // 그룹 이미지 URL (선택)

    @Column
    private Integer memberLimit;            // 최대 멤버 수 (null = 무제한)

    @Column(nullable = false)
    private Long ownerId;                   // 그룹장 User ID

    private LocalDateTime deletedAt;
}
```

#### 3.1.2 GroupMembership (그룹 멤버십)
```java
@Entity(name = "group_memberships")
@SQLDelete(sql = "UPDATE group_memberships SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class GroupMembership extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 15)
    private String nickname;                // 그룹 내 익명 닉네임 (그룹 내 유니크)

    @Column(length = 50)
    private String avatarType;              // 기본 아바타 타입 (시스템 제공)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipRole role;            // OWNER, MEMBER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;        // PENDING, APPROVED, KICKED

    private LocalDateTime joinedAt;         // 승인 시점

    private LocalDateTime deletedAt;
}
```

#### 3.1.3 GroupInviteLink (초대 링크)
```java
@Entity(name = "group_invite_links")
@SQLDelete(sql = "UPDATE group_invite_links SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class GroupInviteLink extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, unique = true, length = 36)
    private String code;                    // UUID 형식

    @Column(nullable = false)
    private LocalDateTime expiresAt;        // 만료 시점 (생성 후 7일)

    @Column(nullable = false)
    private boolean active;                 // 활성화 여부 (그룹장이 비활성화 가능)

    private LocalDateTime deletedAt;
}
```

### 3.2 기존 엔티티 수정

#### 3.2.1 Moment 수정
```java
// 추가 필드
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "group_id", nullable = false)
private Group group;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "membership_id", nullable = false)
private GroupMembership membership;         // 작성자의 그룹 멤버십 (익명 프로필)

// 제거 필드
// - writeType (BASIC/EXTRA 구분 제거 - MVP에서 무제한)
// - isMatched (미사용)
```

#### 3.2.2 Comment 수정
```java
// 추가 필드
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "membership_id", nullable = false)
private GroupMembership membership;         // 작성자의 그룹 멤버십
```

### 3.3 Enum 정의

```java
public enum MembershipRole {
    OWNER,      // 그룹장 (그룹당 1명)
    MEMBER      // 일반 멤버
}

public enum MembershipStatus {
    PENDING,    // 가입 대기 (승인 필요)
    APPROVED,   // 승인됨
    KICKED      // 강퇴됨 (재가입 시 PENDING으로 재생성)
}
```

### 3.4 제거되는 엔티티/기능
- `Echo` 엔티티 및 관련 서비스 (MVP에서 제거)
- `Tag`, `MomentTag` 엔티티 (제거)
- `RewardHistory` 관련 기능 (MVP에서 보류)
- `Level` 시스템 (MVP에서 보류)
- `OnceADayPolicy`, `PointDeductionPolicy` (제거)
- `WriteType` enum (제거)

---

## 4. 데이터베이스 마이그레이션

### 4.1 신규 테이블

```sql
-- V25__create_groups.sql
CREATE TABLE groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    description VARCHAR(200) NOT NULL,
    image_url VARCHAR(2083),
    member_limit INT,
    owner_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    deleted_at DATETIME,

    INDEX idx_groups_owner (owner_id),
    INDEX idx_groups_deleted (deleted_at)
);

-- V26__create_group_memberships.sql
CREATE TABLE group_memberships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    nickname VARCHAR(15) NOT NULL,
    avatar_type VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    joined_at DATETIME,
    created_at DATETIME NOT NULL,
    deleted_at DATETIME,

    UNIQUE KEY uk_membership_group_user (group_id, user_id, deleted_at),
    UNIQUE KEY uk_membership_group_nickname (group_id, nickname, deleted_at),
    INDEX idx_membership_user (user_id),
    INDEX idx_membership_status (group_id, status),

    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- V27__create_group_invite_links.sql
CREATE TABLE group_invite_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    code VARCHAR(36) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    deleted_at DATETIME,

    INDEX idx_invite_code (code),
    INDEX idx_invite_group (group_id),
    INDEX idx_invite_expires (expires_at),

    FOREIGN KEY (group_id) REFERENCES groups(id)
);
```

### 4.2 기존 테이블 수정

```sql
-- V28__alter_moments_for_groups.sql
ALTER TABLE moments
    ADD COLUMN group_id BIGINT,
    ADD COLUMN membership_id BIGINT,
    ADD INDEX idx_moments_group (group_id),
    ADD INDEX idx_moments_membership (membership_id);

-- V29__alter_comments_for_groups.sql
ALTER TABLE comments
    ADD COLUMN membership_id BIGINT,
    ADD INDEX idx_comments_membership (membership_id);

-- V30__soft_delete_legacy_data.sql
-- 기존 데이터 soft delete
UPDATE moments SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE comments SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE moment_tags SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE tags SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE emojis SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE reward_history SET deleted_at = NOW() WHERE deleted_at IS NULL;
```

---

## 5. API 설계 (v2)

### 5.1 그룹 API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v2/groups` | 그룹 생성 | Required |
| GET | `/api/v2/groups` | 내 그룹 목록 조회 | Required |
| GET | `/api/v2/groups/{groupId}` | 그룹 상세 조회 | Required (멤버만) |
| PATCH | `/api/v2/groups/{groupId}` | 그룹 정보 수정 | Required (Owner만) |
| DELETE | `/api/v2/groups/{groupId}` | 그룹 삭제 | Required (Owner만) |

### 5.2 그룹 멤버십 API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v2/groups/join` | 초대 링크로 가입 신청 | Required |
| GET | `/api/v2/groups/{groupId}/members` | 멤버 목록 조회 | Required (멤버만) |
| GET | `/api/v2/groups/{groupId}/pending` | 가입 대기자 목록 | Required (Owner만) |
| PATCH | `/api/v2/groups/{groupId}/members/{membershipId}/approve` | 가입 승인 | Required (Owner만) |
| PATCH | `/api/v2/groups/{groupId}/members/{membershipId}/reject` | 가입 거절 | Required (Owner만) |
| DELETE | `/api/v2/groups/{groupId}/members/{membershipId}` | 멤버 강퇴 | Required (Owner만) |
| DELETE | `/api/v2/groups/{groupId}/leave` | 그룹 탈퇴 | Required |
| PATCH | `/api/v2/groups/{groupId}/transfer/{membershipId}` | 소유권 이전 | Required (Owner만) |
| PATCH | `/api/v2/groups/{groupId}/profile` | 내 그룹 프로필 수정 | Required (멤버만) |

### 5.3 초대 링크 API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v2/groups/{groupId}/invite` | 초대 링크 생성 | Required (Owner만) |
| GET | `/api/v2/groups/{groupId}/invite` | 현재 초대 링크 조회 | Required (Owner만) |
| DELETE | `/api/v2/groups/{groupId}/invite` | 초대 링크 비활성화 | Required (Owner만) |
| GET | `/api/v2/invite/{code}` | 초대 링크 정보 조회 | Required |

### 5.4 모멘트 API (그룹 기반)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v2/groups/{groupId}/moments` | 모멘트 작성 | Required (멤버만) |
| GET | `/api/v2/groups/{groupId}/moments` | 그룹 피드 조회 | Required (멤버만) |
| GET | `/api/v2/groups/{groupId}/moments/{momentId}` | 모멘트 상세 조회 | Required (멤버만) |
| DELETE | `/api/v2/groups/{groupId}/moments/{momentId}` | 모멘트 삭제 | Required (작성자/Owner) |

### 5.5 코멘트 API (그룹 기반)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v2/groups/{groupId}/moments/{momentId}/comments` | 코멘트 작성 | Required (멤버만) |
| GET | `/api/v2/groups/{groupId}/moments/{momentId}/comments` | 코멘트 목록 조회 | Required (멤버만) |
| DELETE | `/api/v2/groups/{groupId}/comments/{commentId}` | 코멘트 삭제 | Required (작성자/Owner) |

### 5.6 신고 API (그룹 기반)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v2/groups/{groupId}/reports` | 신고 접수 | Required (멤버만) |
| GET | `/api/v2/groups/{groupId}/reports` | 신고 목록 조회 | Required (Owner만) |
| PATCH | `/api/v2/groups/{groupId}/reports/{reportId}/resolve` | 신고 처리 | Required (Owner만) |

### 5.7 알림 API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v2/notifications/subscribe` | SSE 구독 | Required |
| GET | `/api/v2/notifications` | 알림 목록 조회 | Required |
| PATCH | `/api/v2/notifications/{id}/read` | 알림 읽음 처리 | Required |

---

## 6. DTO 설계

### 6.1 그룹 관련 DTO

```java
// Request
public record GroupCreateRequest(
    @NotBlank @Size(min = 1, max = 30) String name,
    @NotBlank @Size(min = 1, max = 200) String description,
    String imageUrl,
    @Min(2) Integer memberLimit
) {}

public record GroupUpdateRequest(
    @Size(min = 1, max = 30) String name,
    @Size(min = 1, max = 200) String description,
    String imageUrl,
    @Min(2) Integer memberLimit
) {}

public record GroupJoinRequest(
    @NotBlank String inviteCode,
    @NotBlank @Size(min = 1, max = 15) String nickname
) {}

public record GroupProfileUpdateRequest(
    @NotBlank @Size(min = 1, max = 15) String nickname,
    String avatarType
) {}

// Response
public record GroupResponse(
    Long id,
    String name,
    String description,
    String imageUrl,
    Integer memberLimit,
    Integer memberCount,
    boolean isOwner
) {
    public static GroupResponse from(Group group, int memberCount, boolean isOwner) {
        return new GroupResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            group.getImageUrl(),
            group.getMemberLimit(),
            memberCount,
            isOwner
        );
    }
}

public record GroupListResponse(
    Long id,
    String name,
    String imageUrl,
    Integer memberCount
) {}

public record MemberResponse(
    Long membershipId,
    String nickname,
    String avatarType,
    MembershipRole role,
    LocalDateTime joinedAt
) {}

public record InviteLinkResponse(
    String code,
    String fullUrl,
    LocalDateTime expiresAt,
    boolean active
) {}

public record InviteInfoResponse(
    Long groupId,
    String groupName,
    String groupDescription,
    String groupImageUrl,
    Integer memberCount,
    boolean isExpired
) {}
```

### 6.2 모멘트/코멘트 DTO (v2)

```java
// Request
public record MomentCreateRequestV2(
    @NotBlank @Size(min = 1, max = 200) String content,
    String imageUrl
) {}

public record CommentCreateRequestV2(
    @NotBlank @Size(min = 1, max = 200) String content
) {}

// Response
public record MomentResponseV2(
    Long id,
    String content,
    String imageUrl,
    AuthorResponse author,
    Integer commentCount,
    LocalDateTime createdAt
) {}

public record CommentResponseV2(
    Long id,
    String content,
    AuthorResponse author,
    LocalDateTime createdAt
) {}

public record AuthorResponse(
    Long membershipId,
    String nickname,
    String avatarType,
    boolean isOwner,
    boolean isLeft       // 탈퇴한 멤버 여부
) {}
```

---

## 7. 서비스 계층 설계

### 7.1 신규 서비스

```
group/
├── domain/
│   ├── Group.java
│   ├── GroupMembership.java
│   ├── GroupInviteLink.java
│   ├── MembershipRole.java
│   └── MembershipStatus.java
├── infrastructure/
│   ├── GroupRepository.java
│   ├── GroupMembershipRepository.java
│   └── GroupInviteLinkRepository.java
├── service/
│   ├── group/
│   │   ├── GroupService.java              # 그룹 CRUD
│   │   └── GroupMembershipService.java    # 멤버십 관리
│   ├── application/
│   │   ├── GroupApplicationService.java   # 그룹 생성/수정 조율
│   │   └── GroupJoinApplicationService.java # 가입/승인 프로세스
│   └── facade/
│       └── GroupFacadeService.java        # 그룹 목록, 상세 조회
├── presentation/
│   ├── GroupController.java
│   ├── GroupMemberController.java
│   └── GroupInviteController.java
└── dto/
    ├── request/
    └── response/
```

### 7.2 수정되는 서비스

#### MomentService 수정
- `createMoment(Group, GroupMembership, content, imageUrl)` - 그룹 컨텍스트 추가
- `getMomentsByGroup(Group, cursor, limit)` - 그룹별 피드 조회
- 기존 `OnceADayPolicy`, `PointDeductionPolicy` 제거

#### CommentService 수정
- `createComment(Moment, GroupMembership, content)` - 멤버십 기반 작성
- 7일 제한 로직 제거

#### NotificationService 수정
- 알림에 그룹 정보 포함
- 그룹 관련 이벤트 추가 (가입 신청, 승인, 강퇴 등)

### 7.3 도메인 이벤트

```java
// 기존 이벤트 수정
public record CommentCreateEvent(
    Long momentId,
    Long commentId,
    Long groupId,                // 추가
    Long authorMembershipId,     // 추가
    Long momentAuthorUserId
) {}

// 신규 이벤트
public record GroupJoinRequestEvent(
    Long groupId,
    Long userId,
    Long membershipId
) {}

public record GroupJoinApprovedEvent(
    Long groupId,
    Long userId,
    Long membershipId
) {}

public record GroupMemberKickedEvent(
    Long groupId,
    Long userId,
    Long membershipId
) {}
```

---

## 8. 비즈니스 규칙

### 8.1 그룹 생성
- 이름(1-30자), 설명(1-200자) 필수
- 이미지, 멤버 제한은 선택
- 생성자가 자동으로 OWNER + APPROVED 멤버십 생성
- 생성 시 초대 링크 자동 생성 (7일 유효)

### 8.2 가입 프로세스
1. 사용자가 초대 링크로 접근
2. 그룹 정보 확인 (승인 전이므로 기본 정보만)
3. 닉네임 입력 후 가입 신청 (PENDING 상태)
4. 그룹장에게 알림 발송
5. 그룹장이 승인 → APPROVED, 거절 → 멤버십 삭제
6. 승인 시 사용자에게 알림

### 8.3 닉네임 규칙
- 그룹 내에서만 유니크
- 1-15자
- 그룹별로 다른 닉네임 사용 가능
- 수정 가능 (그룹 내 중복 체크)

### 8.4 그룹장 이전
- 그룹장이 탈퇴 시 소유권 이전 필수
- 이전 없이 탈퇴 시 가장 오래된 APPROVED 멤버에게 자동 이전
- 멤버가 없으면 그룹 soft delete

### 8.5 강퇴 정책
- 그룹장만 강퇴 가능
- 강퇴 시 해당 멤버에게 알림
- 강퇴된 멤버도 재가입 신청 가능 (PENDING으로 새 멤버십)
- 강퇴된 멤버의 콘텐츠는 유지 ("탈퇴한 멤버" 표시)

### 8.6 신고 처리
- 그룹장이 직접 처리 (자동 삭제 없음)
- 신고 내역 조회 및 처리 상태 관리

### 8.7 초대 링크
- 7일 고정 만료
- 그룹장이 비활성화/재생성 가능
- 만료된 링크로 접근 시 에러

---

## 9. 알림 시스템

### 9.1 기존 알림 유지
- NEW_COMMENT_ON_MOMENT (내 모멘트에 새 코멘트)

### 9.2 신규 알림 타입

```java
public enum NotificationType {
    // 기존
    NEW_COMMENT_ON_MOMENT,

    // 신규 (그룹 관련)
    GROUP_JOIN_REQUESTED,       // 가입 신청 (그룹장에게)
    GROUP_JOIN_APPROVED,        // 가입 승인됨 (신청자에게)
    GROUP_JOIN_REJECTED,        // 가입 거절됨 (신청자에게)
    GROUP_MEMBER_KICKED,        // 강퇴됨 (강퇴된 멤버에게)
    GROUP_OWNERSHIP_TRANSFERRED // 소유권 이전됨 (새 그룹장에게)
}
```

### 9.3 알림 데이터 구조

```java
// Notification 엔티티에 그룹 정보 추가
@Column
private Long groupId;

@Column
private Long targetMembershipId;  // 관련 멤버십 ID
```

---

## 10. 보안 및 권한

### 10.1 API 접근 제어

| 권한 | 조건 |
|------|------|
| 그룹 조회/피드 | APPROVED 멤버만 |
| 모멘트/코멘트 작성 | APPROVED 멤버만 |
| 그룹 설정 수정 | OWNER만 |
| 가입 승인/거절 | OWNER만 |
| 멤버 강퇴 | OWNER만 |
| 초대 링크 관리 | OWNER만 |
| 자기 콘텐츠 삭제 | 작성자 본인 또는 OWNER |

### 10.2 권한 검증 로직

```java
@Service
@RequiredArgsConstructor
public class GroupAuthorizationService {

    public void validateMemberAccess(Long groupId, Long userId) {
        GroupMembership membership = membershipRepository
            .findByGroupIdAndUserIdAndStatus(groupId, userId, APPROVED)
            .orElseThrow(() -> new MomentException(ErrorCode.GROUP_ACCESS_DENIED));
    }

    public void validateOwnerAccess(Long groupId, Long userId) {
        GroupMembership membership = membershipRepository
            .findByGroupIdAndUserIdAndRole(groupId, userId, OWNER)
            .orElseThrow(() -> new MomentException(ErrorCode.GROUP_OWNER_REQUIRED));
    }

    public GroupMembership getApprovedMembership(Long groupId, Long userId) {
        return membershipRepository
            .findByGroupIdAndUserIdAndStatus(groupId, userId, APPROVED)
            .orElseThrow(() -> new MomentException(ErrorCode.GROUP_ACCESS_DENIED));
    }
}
```

---

## 11. 에러 코드

### 11.1 신규 에러 코드

```java
// ErrorCode enum에 추가
// Group 관련 (GR-xxx)
GROUP_NOT_FOUND("GR-001", "그룹을 찾을 수 없습니다.", NOT_FOUND),
GROUP_ACCESS_DENIED("GR-002", "그룹에 접근할 수 없습니다.", FORBIDDEN),
GROUP_OWNER_REQUIRED("GR-003", "그룹장 권한이 필요합니다.", FORBIDDEN),
GROUP_MEMBER_LIMIT_EXCEEDED("GR-004", "그룹 최대 인원을 초과했습니다.", BAD_REQUEST),
GROUP_CANNOT_LEAVE_AS_OWNER("GR-005", "그룹장은 소유권 이전 후 탈퇴할 수 있습니다.", BAD_REQUEST),
GROUP_LAST_MEMBER_CANNOT_TRANSFER("GR-006", "다른 멤버가 없어 소유권을 이전할 수 없습니다.", BAD_REQUEST),

// Membership 관련 (GM-xxx)
MEMBERSHIP_NOT_FOUND("GM-001", "멤버십을 찾을 수 없습니다.", NOT_FOUND),
MEMBERSHIP_ALREADY_EXISTS("GM-002", "이미 가입한 그룹입니다.", CONFLICT),
MEMBERSHIP_PENDING("GM-003", "가입 승인 대기 중입니다.", BAD_REQUEST),
MEMBERSHIP_NICKNAME_DUPLICATE("GM-004", "이미 사용 중인 닉네임입니다.", CONFLICT),
MEMBERSHIP_CANNOT_KICK_OWNER("GM-005", "그룹장은 강퇴할 수 없습니다.", BAD_REQUEST),
MEMBERSHIP_CANNOT_KICK_SELF("GM-006", "자신을 강퇴할 수 없습니다.", BAD_REQUEST),

// Invite 관련 (GI-xxx)
INVITE_NOT_FOUND("GI-001", "초대 링크를 찾을 수 없습니다.", NOT_FOUND),
INVITE_EXPIRED("GI-002", "만료된 초대 링크입니다.", BAD_REQUEST),
INVITE_INACTIVE("GI-003", "비활성화된 초대 링크입니다.", BAD_REQUEST)
```

---

## 12. 테스트 전략

### 12.1 단위 테스트

```java
// GroupService 테스트 예시
@Test
void createGroup_성공() {
    // given
    User owner = createUser();
    GroupCreateRequest request = new GroupCreateRequest("테스트 그룹", "설명", null, null);

    // when
    Group group = groupService.createGroup(owner, request);

    // then
    assertThat(group.getName()).isEqualTo("테스트 그룹");
    assertThat(group.getOwnerId()).isEqualTo(owner.getId());
}

@Test
void joinGroup_승인대기상태로_생성() {
    // given
    Group group = createGroup();
    User user = createUser();
    String nickname = "테스터";

    // when
    GroupMembership membership = membershipService.requestJoin(group, user, nickname);

    // then
    assertThat(membership.getStatus()).isEqualTo(MembershipStatus.PENDING);
}
```

### 12.2 E2E 테스트

```java
@Tag("e2e")
class GroupControllerTest extends AcceptanceTest {

    @Test
    void 그룹_생성_및_가입_플로우() {
        // 1. 그룹 생성
        String ownerToken = 로그인("owner@test.com");
        GroupCreateRequest createRequest = new GroupCreateRequest("테스트", "설명", null, null);

        var createResponse = given()
            .header("Authorization", "Bearer " + ownerToken)
            .body(createRequest)
            .post("/api/v2/groups");

        Long groupId = createResponse.jsonPath().getLong("data.id");

        // 2. 초대 링크 조회
        var inviteResponse = given()
            .header("Authorization", "Bearer " + ownerToken)
            .get("/api/v2/groups/" + groupId + "/invite");

        String inviteCode = inviteResponse.jsonPath().getString("data.code");

        // 3. 다른 사용자가 가입 신청
        String userToken = 로그인("user@test.com");
        GroupJoinRequest joinRequest = new GroupJoinRequest(inviteCode, "새멤버");

        given()
            .header("Authorization", "Bearer " + userToken)
            .body(joinRequest)
            .post("/api/v2/groups/join")
            .then()
            .statusCode(201);

        // 4. 그룹장이 승인
        // ... 계속
    }
}
```

---

## 13. 구현 우선순위

### Phase 1: 핵심 인프라 (Day 1-3)
1. [ ] DB 마이그레이션 스크립트 작성
2. [ ] Group, GroupMembership, GroupInviteLink 엔티티 구현
3. [ ] Repository 구현
4. [ ] 기본 서비스 레이어 구현

### Phase 2: 그룹 관리 API (Day 4-6)
1. [ ] 그룹 생성/수정/삭제 API
2. [ ] 초대 링크 생성/조회/비활성화 API
3. [ ] 가입 신청/승인/거절 API
4. [ ] 멤버 목록/강퇴/탈퇴 API
5. [ ] 소유권 이전 API

### Phase 3: 콘텐츠 API (Day 7-9)
1. [ ] 그룹 기반 모멘트 CRUD
2. [ ] 그룹 기반 코멘트 CRUD
3. [ ] 그룹 피드 조회 (커서 페이지네이션)
4. [ ] 신고 API

### Phase 4: 알림 & 통합 (Day 10-11)
1. [ ] 알림 시스템 수정 (그룹 컨텍스트 추가)
2. [ ] 그룹 관련 이벤트/알림 추가
3. [ ] 기존 v1 API 정리

### Phase 5: 테스트 & 배포 (Day 12-14)
1. [ ] 단위 테스트 작성
2. [ ] E2E 테스트 작성
3. [ ] 레거시 데이터 마이그레이션 실행
4. [ ] 배포 및 모니터링

---

## 14. 향후 확장 계획 (MVP 이후)

### 14.1 Star/Level 시스템 재도입
- 그룹별 독립 포인트 vs 통합 포인트 결정 필요
- 레벨 시스템 재설계

### 14.2 작성 제한 정책
- 그룹장이 설정 가능한 작성 빈도 제한
- 하루 N회, 주 N회 등 옵션

### 14.3 Echo(반응) 시스템 재도입
- 코멘트에 대한 이모지 반응
- 그룹 커스텀 이모지 가능성

### 14.4 공개 그룹 / 탐색 기능
- 공개/비공개 그룹 구분
- 공개 그룹 검색/탐색 기능

### 14.5 초대 링크 고급 기능
- 사용 횟수 제한
- 커스텀 만료 기간 설정

---

## 15. 체크리스트

### 배포 전 확인사항
- [ ] 모든 마이그레이션 스크립트 검증
- [ ] 레거시 데이터 백업 완료
- [ ] v2 API 모든 엔드포인트 테스트
- [ ] 권한 검증 로직 검증
- [ ] 에러 핸들링 검증
- [ ] 알림 발송 테스트
- [ ] 부하 테스트 (그룹당 최대 멤버 시나리오)

### 롤백 계획
- 레거시 데이터는 soft delete만 하므로 복구 가능
- v1 API는 당분간 유지하여 긴급 시 롤백 가능
