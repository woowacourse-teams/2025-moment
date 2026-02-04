# Group Domain (PREFIX: GRP)

> Last Updated: 2026-02-04
> Features: 29

## 컨트롤러 매핑

| Controller | Base Path | 기능 수 |
|------------|-----------|---------|
| `GroupController` | `/api/v2/groups` | 5 |
| `GroupMemberController` | `/api/v2/groups/{groupId}` | 4 |
| `GroupMemberApprovalController` | `/api/v2/groups/{groupId}` | 4 |
| `GroupInviteController` | `/api/v2` | 3 |
| `GroupMomentController` | `/api/v2/groups/{groupId}` | 7 |
| `GroupCommentController` | `/api/v2/groups/{groupId}` | 6 |

## 기능 목록

### 그룹 관리 (GRP-001 ~ GRP-005)

### GRP-001: 그룹 생성

- **Status**: DONE
- **API**: `POST /api/v2/groups`
- **Key Classes**:
    - Controller: `GroupController`
    - Application: `GroupApplicationService`
    - Domain: `GroupService`
    - Entity: `Group`
- **Business Rules**: 그룹 생성 시 작성자가 OWNER로 자동 가입
- **Tests**: `GroupTest`, `GroupServiceTest`, `GroupControllerTest` (E2E)

### GRP-002: 그룹 목록 조회

- **Status**: DONE
- **API**: `GET /api/v2/groups`
- **Key Classes**:
    - Controller: `GroupController`
    - Application: `GroupApplicationService`
- **Business Rules**: 내가 속한 그룹 목록 반환
- **Tests**: `GroupControllerTest` (E2E)

### GRP-003: 그룹 상세 조회

- **Status**: DONE
- **API**: `GET /api/v2/groups/{groupId}`
- **Key Classes**:
    - Controller: `GroupController`
    - Application: `GroupApplicationService`
- **Business Rules**: 그룹 멤버만 조회 가능
- **Tests**: `GroupControllerTest` (E2E)
- **Error Codes**: GR-001 (존재하지 않는 그룹)

### GRP-004: 그룹 수정

- **Status**: DONE
- **API**: `PUT /api/v2/groups/{groupId}`
- **Key Classes**:
    - Controller: `GroupController`
    - Domain: `GroupService`
- **Business Rules**: OWNER만 수정 가능
- **Tests**: `GroupControllerTest` (E2E)
- **Error Codes**: GR-002

### GRP-005: 그룹 삭제

- **Status**: DONE
- **API**: `DELETE /api/v2/groups/{groupId}`
- **Key Classes**:
    - Controller: `GroupController`
    - Application: `GroupApplicationService`
- **Business Rules**: OWNER만 삭제 가능, Soft Delete
- **Tests**: `GroupControllerTest` (E2E)
- **Error Codes**: GR-003

### 멤버 관리 (GRP-006 ~ GRP-013)

### GRP-006: 멤버 목록 조회

- **Status**: DONE
- **API**: `GET /api/v2/groups/{groupId}/members`
- **Key Classes**:
    - Controller: `GroupMemberController`
    - Application: `GroupMemberApplicationService`
- **Tests**: `GroupMemberControllerTest` (E2E)

### GRP-007: 가입 대기 목록 조회

- **Status**: DONE
- **API**: `GET /api/v2/groups/{groupId}/pending-members`
- **Key Classes**:
    - Controller: `GroupMemberController`
    - Application: `GroupMemberApplicationService`
- **Business Rules**: OWNER만 조회 가능
- **Tests**: `GroupMemberControllerTest` (E2E)

### GRP-008: 멤버 프로필 조회

- **Status**: DONE
- **API**: `GET /api/v2/groups/{groupId}/members/me`
- **Key Classes**:
    - Controller: `GroupMemberController`
    - Application: `GroupMemberApplicationService`
- **Tests**: `GroupMemberControllerTest` (E2E)

### GRP-009: 그룹 탈퇴

- **Status**: DONE
- **API**: `DELETE /api/v2/groups/{groupId}/members/me`
- **Key Classes**:
    - Controller: `GroupMemberController`
    - Domain: `GroupMemberService`
- **Business Rules**: OWNER는 탈퇴 불가 (소유권 이전 먼저 필요)
- **Tests**: `GroupMemberTest`, `GroupMemberServiceTest`, `GroupMemberControllerTest` (E2E)
- **Error Codes**: GM-001 ~ GM-003

### GRP-010: 멤버 강퇴

- **Status**: DONE
- **API**: `DELETE /api/v2/groups/{groupId}/members/{memberId}`
- **Key Classes**:
    - Controller: `GroupMemberApprovalController`
    - Domain: `GroupMemberService`
- **Business Rules**: OWNER만 강퇴 가능
- **Tests**: `GroupMemberServiceTest`, `GroupMemberApplicationServiceTest`
- **Notes**: User API E2E 테스트 (`GroupMemberApprovalControllerTest`) 미존재
- **Error Codes**: GM-004 ~ GM-005

### GRP-011: 가입 승인

- **Status**: DONE
- **API**: `POST /api/v2/groups/{groupId}/members/{memberId}/approve`
- **Key Classes**:
    - Controller: `GroupMemberApprovalController`
    - Application: `GroupMemberApplicationService`
- **Business Rules**: OWNER만 승인 가능
- **Tests**: `GroupMemberApplicationServiceTest`
- **Notes**: User API E2E 테스트 미존재
- **Error Codes**: GM-006

### GRP-012: 가입 거절

- **Status**: DONE
- **API**: `POST /api/v2/groups/{groupId}/members/{memberId}/reject`
- **Key Classes**:
    - Controller: `GroupMemberApprovalController`
    - Application: `GroupMemberApplicationService`
- **Business Rules**: OWNER만 거절 가능
- **Tests**: `GroupMemberApplicationServiceTest`
- **Notes**: User API E2E 테스트 미존재
- **Error Codes**: GM-007

### GRP-013: 소유권 이전

- **Status**: DONE
- **API**: `POST /api/v2/groups/{groupId}/transfer-ownership/{memberId}`
- **Key Classes**:
    - Controller: `GroupMemberApprovalController`
    - Domain: `GroupMemberService`
- **Business Rules**: OWNER만 이전 가능, 대상은 MEMBER 상태여야 함
- **Tests**: `GroupMemberServiceTest`
- **Notes**: User API E2E 테스트 미존재
- **Error Codes**: GM-008

### 초대 (GRP-014 ~ GRP-016)

### GRP-014: 초대 링크 생성

- **Status**: DONE
- **API**: `POST /api/v2/groups/{groupId}/invite-link`
- **Key Classes**:
    - Controller: `GroupInviteController`
    - Domain: `InviteLinkService`
    - Entity: `GroupInviteLink`
- **Business Rules**: 그룹 멤버만 초대 링크 생성 가능
- **Tests**: `GroupInviteLinkTest`, `InviteLinkServiceTest`, `GroupInviteControllerTest` (E2E)
- **Error Codes**: IL-001

### GRP-015: 초대 링크 조회

- **Status**: DONE
- **API**: `GET /api/v2/invite/{code}`
- **Key Classes**:
    - Controller: `GroupInviteController`
    - Domain: `InviteLinkService`
- **Business Rules**: 코드로 그룹 정보 조회
- **Tests**: `GroupInviteControllerTest` (E2E)
- **Error Codes**: IL-002

### GRP-016: 초대 코드로 가입

- **Status**: DONE
- **API**: `POST /api/v2/invite/{code}/join`
- **Key Classes**:
    - Controller: `GroupInviteController`
    - Application: `GroupMemberApplicationService`
- **Business Rules**: 초대 코드 유효성 검증, 중복 가입 방지
- **Tests**: `GroupMemberApplicationServiceTest`, `GroupInviteControllerTest` (E2E)

### 그룹 모멘트 (GRP-017 ~ GRP-023)

### GRP-017: 그룹 모멘트 생성

- **Status**: DONE
- **API**: `POST /api/v2/groups/{groupId}/moments`
- **Key Classes**:
    - Controller: `GroupMomentController`
    - Facade: `MomentCreateFacadeService`
- **Tests**: `GroupMomentControllerTest` (E2E)

### GRP-018: 그룹 모멘트 목록 조회

- **Status**: DONE
- **API**: `GET /api/v2/groups/{groupId}/moments`
- **Key Classes**:
    - Controller: `GroupMomentController`
    - Facade: `MyGroupMomentPageFacadeService`
- **Business Rules**: 커서 기반 페이지네이션
- **Tests**: `MyGroupMomentPageFacadeServiceTest`, `GroupMomentControllerTest` (E2E)

### GRP-019: 내 그룹 모멘트 조회

- **Status**: DONE
- **API**: `GET /api/v2/groups/{groupId}/moments/me`
- **Key Classes**:
    - Controller: `GroupMomentController`
- **Tests**: `GroupMomentControllerTest` (E2E)

### GRP-020: 안읽은 모멘트 수 조회

- **Status**: DONE
- **API**: `GET /api/v2/groups/{groupId}/moments/unread`
- **Key Classes**:
    - Controller: `GroupMomentController`
- **Tests**: `GroupMomentControllerTest` (E2E)

### GRP-021: 그룹 모멘트 삭제

- **Status**: DONE
- **API**: `DELETE /api/v2/groups/{groupId}/moments/{momentId}`
- **Key Classes**:
    - Controller: `GroupMomentController`
- **Business Rules**: 작성자 본인만 삭제 가능
- **Tests**: `GroupMomentControllerTest` (E2E)

### GRP-022: 그룹 모멘트 좋아요

- **Status**: DONE
- **API**: `POST /api/v2/groups/{groupId}/moments/{momentId}/like`
- **Key Classes**:
    - Controller: `GroupMomentController`
    - Domain: `MomentLikeService`
- **Notes**: LIK-001과 동일 엔드포인트
- **Tests**: `MomentLikeServiceTest`

### GRP-023: 댓글 가능 여부 확인

- **Status**: DONE
- **API**: `GET /api/v2/groups/{groupId}/moments/{momentId}/commentable`
- **Key Classes**:
    - Controller: `GroupMomentController`
- **Tests**: `GroupMomentControllerTest` (E2E)

### 그룹 댓글 (GRP-024 ~ GRP-029)

### GRP-024: 그룹 댓글 생성

- **Status**: DONE
- **API**: `POST /api/v2/groups/{groupId}/moments/{momentId}/comments`
- **Key Classes**:
    - Controller: `GroupCommentController`
    - Facade: `CommentCreateFacadeService`
- **Tests**: `GroupCommentControllerTest` (E2E)

### GRP-025: 그룹 댓글 목록 조회

- **Status**: DONE
- **API**: `GET /api/v2/groups/{groupId}/moments/{momentId}/comments`
- **Key Classes**:
    - Controller: `GroupCommentController`
    - Facade: `MyGroupCommentPageFacadeService`
- **Business Rules**: 커서 기반 페이지네이션
- **Tests**: `MyGroupCommentPageFacadeServiceTest`, `GroupCommentControllerTest` (E2E)

### GRP-026: 그룹 댓글 삭제

- **Status**: DONE
- **API**: `DELETE /api/v2/groups/{groupId}/comments/{commentId}`
- **Key Classes**:
    - Controller: `GroupCommentController`
- **Business Rules**: 작성자 본인만 삭제 가능
- **Tests**: `GroupCommentControllerTest` (E2E)

### GRP-027: 그룹 댓글 좋아요

- **Status**: DONE
- **API**: `POST /api/v2/groups/{groupId}/comments/{commentId}/like`
- **Key Classes**:
    - Controller: `GroupCommentController`
    - Domain: `CommentLikeService`
- **Notes**: LIK-002와 동일 엔드포인트
- **Tests**: `CommentLikeServiceTest`

### GRP-028: 내 그룹 댓글 조회

- **Status**: DONE
- **API**: `GET /api/v2/groups/{groupId}/comments/me`
- **Key Classes**:
    - Controller: `GroupCommentController`
- **Tests**: `GroupCommentControllerTest` (E2E)

### GRP-029: 안읽은 댓글 수 조회

- **Status**: DONE
- **API**: `GET /api/v2/groups/{groupId}/comments/unread`
- **Key Classes**:
    - Controller: `GroupCommentController`
- **Tests**: `GroupCommentControllerTest` (E2E)

## Domain Events Published

| Event | 구독자 | 설명 |
|-------|--------|------|
| `GroupJoinRequestEvent` | `NotificationEventHandler` | 가입 요청 시 OWNER에게 알림 |
| `GroupJoinApprovedEvent` | `NotificationEventHandler` | 가입 승인 시 요청자에게 알림 |
| `GroupKickedEvent` | `NotificationEventHandler` | 강퇴 시 대상자에게 알림 |
| `GroupCommentCreateEvent` | `NotificationEventHandler` | 그룹 댓글 생성 시 모멘트 작성자에게 알림 |

## 관련 에러 코드

- GR-001 ~ GR-003: 그룹 관련
- GM-001 ~ GM-008: 멤버 관련
- IL-001 ~ IL-002: 초대 링크 관련

## 관련 엔티티

- `Group` (@Entity: "moment_groups")
- `GroupMember` (@Entity: "group_members") - MemberRole, MemberStatus enums
- `GroupInviteLink` (@Entity: "group_invite_links")

## 관련 테스트 클래스 (15개)

**Domain**: `GroupTest`, `GroupMemberTest`, `GroupInviteLinkTest`, `MemberRoleTest`, `MemberStatusTest`
**Service**: `GroupServiceTest`, `GroupMemberServiceTest`, `InviteLinkServiceTest`, `GroupApplicationServiceTest`, `GroupMemberApplicationServiceTest`
**E2E**: `GroupControllerTest`, `GroupMemberControllerTest`, `GroupInviteControllerTest`, `GroupMomentControllerTest`, `GroupCommentControllerTest`

## DB 마이그레이션

- V27: `V27__create_groups.sql`
- V28: `V28__create_group_members.sql`
- V29: `V29__create_group_invite_links.sql`
- V30: `V30__alter_moments_for_groups.sql`
- V31: `V31__alter_comments_for_groups.sql`
- V32: `V32__create_likes.sql`
- V33: `V33__alter_notifications_for_groups.sql`
- V34: `V34__rename_groups_to_moment_groups.sql`
