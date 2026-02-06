# Admin Domain (PREFIX: ADM)

> Last Updated: 2026-02-03
> Features: 34

## 컨트롤러 매핑

| Controller | Base Path | 기능 수 |
|------------|-----------|---------|
| `AdminAuthApiController` | `/api/admin/auth` | 3 |
| `AdminUserApiController` | `/api/admin/users` | 4 |
| `AdminGroupApiController` | `/api/admin/groups` | 18 |
| `AdminAccountApiController` | `/api/admin/accounts` | 4 |
| `AdminSessionApiController` | `/api/admin/sessions` | 5 |

## 기능 목록

### 관리자 인증 (ADM-001 ~ ADM-003)

### ADM-001: 관리자 로그인

- **Status**: DONE
- **API**: `POST /api/admin/auth/login`
- **Key Classes**:
    - Controller: `AdminAuthApiController`
    - Domain: `AdminService`
    - Infrastructure: `AdminSessionManager`
- **Business Rules**: 세션 기반 인증, session fixation 방지 (changeSessionId)
- **Tests**: `AdminServiceTest`

### ADM-002: 관리자 로그아웃

- **Status**: DONE
- **API**: `POST /api/admin/auth/logout`
- **Key Classes**:
    - Controller: `AdminAuthApiController`
    - Infrastructure: `AdminSessionManager`
- **Business Rules**: 세션 무효화
- **Tests**: `AdminServiceTest`

### ADM-003: 현재 관리자 조회

- **Status**: DONE
- **API**: `GET /api/admin/auth/me`
- **Key Classes**:
    - Controller: `AdminAuthApiController`
    - Domain: `AdminService`
- **Tests**: `AdminServiceTest`

### 사용자 관리 (ADM-004 ~ ADM-007)

### ADM-004: 사용자 목록 조회

- **Status**: DONE
- **API**: `GET /api/admin/users`
- **Key Classes**:
    - Controller: `AdminUserApiController`
    - Domain: `AdminUserService`
- **Business Rules**: 오프셋 기반 페이지네이션, 삭제된 사용자 포함
- **Tests**: `AdminUserServiceTest`

### ADM-005: 사용자 상세 조회

- **Status**: DONE
- **API**: `GET /api/admin/users/{userId}`
- **Key Classes**:
    - Controller: `AdminUserApiController`
    - Domain: `AdminUserService`
- **Tests**: `AdminUserServiceTest`

### ADM-006: 사용자 수정

- **Status**: DONE
- **API**: `PUT /api/admin/users/{userId}`
- **Key Classes**:
    - Controller: `AdminUserApiController`
    - Domain: `AdminUserService`
- **Tests**: `AdminUserServiceTest`

### ADM-007: 사용자 삭제

- **Status**: DONE
- **API**: `DELETE /api/admin/users/{userId}`
- **Key Classes**:
    - Controller: `AdminUserApiController`
    - Domain: `AdminUserService`
- **Business Rules**: Soft Delete
- **Tests**: `AdminUserServiceTest`

### 그룹 관리 (ADM-008 ~ ADM-025)

### ADM-008: 그룹 통계 조회

- **Status**: DONE
- **API**: `GET /api/admin/groups/stats`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupService`
- **Tests**: `AdminGroupServiceTest`, `AdminGroupStatsApiTest` (E2E)

### ADM-009: 그룹 목록 조회

- **Status**: DONE
- **API**: `GET /api/admin/groups`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupService`
- **Business Rules**: 오프셋 기반 페이지네이션
- **Tests**: `AdminGroupServiceTest`, `AdminGroupListApiTest` (E2E)

### ADM-010: 그룹 상세 조회

- **Status**: DONE
- **API**: `GET /api/admin/groups/{groupId}`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupService`
- **Tests**: `AdminGroupServiceTest`, `AdminGroupDetailApiTest` (E2E)
- **Error Codes**: AG-001 (그룹 없음)

### ADM-011: 그룹 멤버 목록 조회

- **Status**: DONE
- **API**: `GET /api/admin/groups/{groupId}/members`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupMemberService`
- **Tests**: `AdminGroupMemberServiceTest`, `AdminGroupMemberApiTest` (E2E)

### ADM-012: 그룹 대기 멤버 목록 조회

- **Status**: DONE
- **API**: `GET /api/admin/groups/{groupId}/pending-members`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupMemberService`
- **Tests**: `AdminGroupMemberServiceTest`

### ADM-013: 그룹 수정

- **Status**: DONE
- **API**: `PUT /api/admin/groups/{groupId}`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupService`
- **Tests**: `AdminGroupServiceTest`, `AdminGroupUpdateApiTest` (E2E)

### ADM-014: 그룹 삭제

- **Status**: DONE
- **API**: `DELETE /api/admin/groups/{groupId}`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupService`
- **Business Rules**: Soft Delete, 관련 멤버/콘텐츠도 함께 처리
- **Tests**: `AdminGroupServiceTest`, `AdminGroupDeleteApiTest` (E2E)

### ADM-015: 그룹 복원

- **Status**: DONE
- **API**: `POST /api/admin/groups/{groupId}/restore`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupService`
- **Tests**: `AdminGroupServiceTest`, `AdminGroupRestoreApiTest` (E2E)

### ADM-016: 멤버 승인

- **Status**: DONE
- **API**: `POST /api/admin/groups/{groupId}/members/{memberId}/approve`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupMemberService`
- **Tests**: `AdminGroupMemberServiceTest`, `AdminMemberApproveApiTest` (E2E)
- **Error Codes**: AM-001 ~ AM-003

### ADM-017: 멤버 거절

- **Status**: DONE
- **API**: `POST /api/admin/groups/{groupId}/members/{memberId}/reject`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupMemberService`
- **Tests**: `AdminGroupMemberServiceTest`, `AdminMemberRejectApiTest` (E2E)

### ADM-018: 멤버 강퇴

- **Status**: DONE
- **API**: `DELETE /api/admin/groups/{groupId}/members/{memberId}`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupMemberService`
- **Tests**: `AdminGroupMemberServiceTest`, `AdminMemberKickApiTest` (E2E)
- **Error Codes**: AM-004 ~ AM-005

### ADM-019: 소유권 이전

- **Status**: DONE
- **API**: `POST /api/admin/groups/{groupId}/transfer-ownership/{id}`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupMemberService`
- **Tests**: `AdminGroupMemberServiceTest`, `AdminOwnershipTransferApiTest` (E2E)
- **Error Codes**: AM-006 ~ AM-007

### ADM-020: 초대 링크 조회

- **Status**: DONE
- **API**: `GET /api/admin/groups/{groupId}/invite-link`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupService`
- **Tests**: `AdminInviteLinkApiTest` (E2E)

### ADM-021: 그룹 로그 조회

- **Status**: DONE
- **API**: `GET /api/admin/groups/logs`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminGroupLogService`
    - Entity: `AdminGroupLog`
- **Tests**: `AdminGroupLogServiceTest`, `AdminGroupLogTest`, `AdminGroupLogApiTest` (E2E)

### ADM-022: 그룹 모멘트 목록 조회

- **Status**: DONE
- **API**: `GET /api/admin/groups/{groupId}/moments`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminContentService`
- **Tests**: `AdminContentServiceTest`, `AdminMomentListApiTest` (E2E)

### ADM-023: 그룹 모멘트 삭제

- **Status**: DONE
- **API**: `DELETE /api/admin/groups/{groupId}/moments/{momentId}`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminContentService`
- **Tests**: `AdminContentServiceTest`, `AdminMomentDeleteApiTest` (E2E)
- **Error Codes**: AC-001 ~ AC-002

### ADM-024: 그룹 댓글 목록 조회

- **Status**: DONE
- **API**: `GET /api/admin/groups/{groupId}/moments/{momentId}/comments`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminContentService`
- **Tests**: `AdminContentServiceTest`, `AdminCommentListApiTest` (E2E)

### ADM-025: 그룹 댓글 삭제

- **Status**: DONE
- **API**: `DELETE /api/admin/groups/{groupId}/comments/{commentId}`
- **Key Classes**:
    - Controller: `AdminGroupApiController`
    - Domain: `AdminContentService`
- **Tests**: `AdminContentServiceTest`, `AdminCommentDeleteApiTest` (E2E)
- **Error Codes**: AC-003 ~ AC-004

### 관리자 계정 관리 (ADM-026 ~ ADM-029)

### ADM-026: 관리자 계정 목록 조회

- **Status**: DONE
- **API**: `GET /api/admin/accounts`
- **Key Classes**:
    - Controller: `AdminAccountApiController`
    - Domain: `AdminService`
- **Business Rules**: SUPER_ADMIN만 접근 가능
- **Tests**: `AdminServiceTest`

### ADM-027: 관리자 계정 생성

- **Status**: DONE
- **API**: `POST /api/admin/accounts`
- **Key Classes**:
    - Controller: `AdminAccountApiController`
    - Domain: `AdminService`
    - Entity: `Admin`
- **Business Rules**: SUPER_ADMIN만 생성 가능
- **Tests**: `AdminTest`, `AdminServiceTest`

### ADM-028: 관리자 계정 차단

- **Status**: DONE
- **API**: `POST /api/admin/accounts/{adminId}/block`
- **Key Classes**:
    - Controller: `AdminAccountApiController`
    - Domain: `AdminService`
- **Business Rules**: SUPER_ADMIN만 차단 가능
- **Tests**: `AdminServiceTest`
- **Error Codes**: A-001 ~ A-005

### ADM-029: 관리자 계정 해제

- **Status**: DONE
- **API**: `POST /api/admin/accounts/{adminId}/unblock`
- **Key Classes**:
    - Controller: `AdminAccountApiController`
    - Domain: `AdminService`
- **Business Rules**: SUPER_ADMIN만 해제 가능
- **Tests**: `AdminServiceTest`

### 세션 관리 (ADM-030 ~ ADM-034)

### ADM-030: 세션 목록 조회

- **Status**: DONE
- **API**: `GET /api/admin/sessions`
- **Key Classes**:
    - Controller: `AdminSessionApiController`
    - Domain: `AdminSessionService`
    - Entity: `AdminSession`
- **Business Rules**: SUPER_ADMIN만 접근 가능
- **Tests**: `AdminSessionServiceTest`

### ADM-031: 세션 상세 조회

- **Status**: DONE
- **API**: `GET /api/admin/sessions/{sessionId}`
- **Key Classes**:
    - Controller: `AdminSessionApiController`
    - Domain: `AdminSessionService`
- **Tests**: `AdminSessionServiceTest`

### ADM-032: 세션 무효화

- **Status**: DONE
- **API**: `DELETE /api/admin/sessions/{sessionId}`
- **Key Classes**:
    - Controller: `AdminSessionApiController`
    - Domain: `AdminSessionService`
- **Tests**: `AdminSessionServiceTest`

### ADM-033: 관리자 전체 세션 무효화

- **Status**: DONE
- **API**: `DELETE /api/admin/sessions/admin/{adminId}`
- **Key Classes**:
    - Controller: `AdminSessionApiController`
    - Domain: `AdminSessionService`
- **Tests**: `AdminSessionServiceTest`

### ADM-034: 세션 이력 조회

- **Status**: DONE
- **API**: `GET /api/admin/sessions/history`
- **Key Classes**:
    - Controller: `AdminSessionApiController`
    - Domain: `AdminSessionService`
- **Tests**: `AdminSessionServiceTest`

## 인증 방식

세션 기반: `AdminAuthInterceptor` + `AdminSessionManager`
- DB에 세션 활성 상태 저장
- SUPER_ADMIN 전용 경로 제어
- session fixation 방지

## 에러 코드 (AdminErrorCode)

- A-001 ~ A-011: 관리자 인증/권한
- AG-001 ~ AG-003: 그룹 관련
- AM-001 ~ AM-007: 멤버 관련
- AC-001 ~ AC-004: 콘텐츠 관련
- A-500: 서버 내부 오류

## 관련 엔티티

- `Admin` (@Entity: "admins") - AdminRole enum (SUPER_ADMIN, ADMIN)
- `AdminSession` (@Entity: "admin_sessions")
- `AdminGroupLog` (@Entity: "admin_group_logs") - AdminGroupLogType enum

## 관련 테스트 클래스 (29개)

**Service**: `AdminServiceTest`, `AdminUserServiceTest`, `AdminGroupServiceTest`, `AdminGroupMemberServiceTest`, `AdminGroupLogServiceTest`, `AdminContentServiceTest`, `AdminSessionServiceTest`, `AdminSessionManagerTest`, `ClientIpExtractorTest`, `UserAgentParserTest`
**Domain**: `AdminTest`, `AdminGroupLogTest`
**E2E** (17개): `AdminGroupListApiTest`, `AdminGroupDetailApiTest`, `AdminGroupStatsApiTest`, `AdminGroupDeleteApiTest`, `AdminGroupRestoreApiTest`, `AdminGroupUpdateApiTest`, `AdminGroupMemberApiTest`, `AdminMemberApproveApiTest`, `AdminMemberRejectApiTest`, `AdminMemberKickApiTest`, `AdminOwnershipTransferApiTest`, `AdminInviteLinkApiTest`, `AdminGroupLogApiTest`, `AdminMomentListApiTest`, `AdminMomentDeleteApiTest`, `AdminCommentListApiTest`, `AdminCommentDeleteApiTest`

## DB 마이그레이션

- V23: `V23__create_admin_table__mysql.sql`
- V24: `V24__create_admin_sessions.sql`
- V35: `V35__create_admin_group_logs.sql`
