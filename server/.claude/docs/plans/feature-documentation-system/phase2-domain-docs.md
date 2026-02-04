# Phase 2: 도메인별 상세 문서 생성 (11개 파일)

> Status: PENDING
> Parent Plan: feature-documentation-system.md

## 목표

각 도메인 모듈의 모든 기능을 상세히 문서화 (비즈니스 규칙, 에러 코드, 테스트 클래스, DB 마이그레이션)

## 생성 파일

`.claude/docs/features/{domain}.md` x 11개 (신규)

## 공통 문서 포맷

각 기능 항목은 다음 포맷 준수:

```markdown
### {FEATURE-ID}: {Feature Name}
- **Status**: DONE
- **API**: `METHOD /api/v2/path`
- **Key Classes**:
  - Controller: `{Class}Controller`
  - Facade: `{Class}FacadeService` (있는 경우)
  - Application: `{Class}ApplicationService` (있는 경우)
  - Domain: `{Class}Service`
  - Entity: `{Entity}.java`
  - DTO: `{Request}.java`, `{Response}.java`
- **Business Rules**: 주요 비즈니스 규칙
- **Dependencies**: 타 도메인 의존성
- **Tests**: 관련 테스트 클래스
- **DB Migration**: V{N}__{description}.sql
- **Error Codes**: 관련 에러 코드
- **Notes**: 참고사항
```

---

## 2-1. auth.md (PREFIX: AUTH, 11 features, ~150줄)

### 기능 목록

| ID | 기능 | API | Key Classes |
|----|------|-----|-------------|
| AUTH-001 | 이메일 로그인 | `POST /api/v2/auth/login` | AuthController, AuthService |
| AUTH-002 | 로그아웃 | `POST /api/v2/auth/logout` | AuthController, AuthService |
| AUTH-003 | Google OAuth | `GET /api/v2/auth/login/google`, `GET /api/v2/auth/callback/google` | AuthController, GoogleAuthService |
| AUTH-004 | Apple Sign-in | `POST /api/v2/auth/apple` | AuthController, AppleAuthService |
| AUTH-005 | 로그인 상태 확인 | `GET /api/v2/auth/login/check` | AuthController, AuthService |
| AUTH-006 | 토큰 갱신 | `POST /api/v2/auth/refresh` | AuthController, AuthService |
| AUTH-007 | 이메일 인증 요청 | `POST /api/v2/auth/email` | AuthController, AuthEmailService |
| AUTH-008 | 이메일 인증 확인 | `POST /api/v2/auth/email/verify` | AuthController, AuthEmailService |
| AUTH-009 | 비밀번호 재설정 요청 | `POST /api/v2/auth/email/password` | AuthController, AuthEmailService |
| AUTH-010 | 비밀번호 재설정 실행 | `POST /api/v2/auth/email/password/reset` | AuthController, AuthEmailService |
| AUTH-011 | JWT Cookie 인증 체계 | N/A (인프라) | LoginUserArgumentResolver, JwtTokenManager, Authentication |

### 관련 에러 코드

- T-001 ~ T-007: 토큰 관련
- U-002: 로그인 실패
- V-001 ~ V-004: 이메일 인증 관련
- AP-001 ~ AP-005: Apple 인증 관련

### 관련 테스트 클래스 (11개)

- `AppleAuthServiceTest`, `TokensIssuerTest`
- `EmailVerificationTest`, `RefreshTokenTest`
- `ApplePublicKeyTest`, `ApplePublicKeysTest`, `AppleUserInfoTest`
- `AppleAuthClientTest`, `JwtTokenManagerTest`
- `RefreshTokenRepositoryTest`
- `AuthControllerTest` (E2E)

### 관련 엔티티

- `RefreshToken` (@Entity: "refresh_tokens")
- `EmailVerification` (Value Object)

### DB 마이그레이션

- V9: `V9__create_refreshToken__mysql.sql`

---

## 2-2. user.md (PREFIX: USER, 7 features, ~100줄)

### 기능 목록

| ID | 기능 | API | Key Classes |
|----|------|-----|-------------|
| USER-001 | 이메일 회원가입 | `POST /api/v2/users/signup` | UserController, UserService |
| USER-002 | 현재 사용자 조회 | `GET /api/v2/users/me` | UserController, UserService |
| USER-003 | 닉네임 중복 확인 | `POST /api/v2/users/signup/nickname/check` | UserController, UserService |
| USER-004 | 랜덤 닉네임 생성 | `GET /api/v2/users/signup/nickname` | UserController, NicknameGenerateApplicationService |
| USER-005 | 마이페이지 프로필 조회 | `GET /api/v2/me/profile` | MyPageController, MyPageFacadeService |
| USER-006 | 닉네임 변경 | `POST /api/v2/me/nickname` | MyPageController, UserService |
| USER-007 | 비밀번호 변경 | `POST /api/v2/me/password` | MyPageController, UserService |

### 관련 에러 코드

- U-001: 이미 가입된 사용자 (409)
- U-002: 로그인 실패 (400)
- U-003: 닉네임 중복 (409)
- U-004 ~ U-006: 유효성 검증 (400)
- U-007: 비밀번호 불일치 (400)
- U-008: 권한 없음 (401)
- U-009: 존재하지 않는 사용자 (404)
- U-010: 닉네임 생성 불가 (409)
- U-012: 비밀번호 동일 (400)
- U-013: 소셜 로그인 비밀번호 변경 불가 (400)

### 관련 테스트 클래스 (7개)

- `UserTest`, `MomentRandomNicknameGeneratorTest`
- `UserRepositoryTest`
- `NicknameGenerateApplicationServiceTest`, `UserServiceTest`
- `MyPageControllerTest` (E2E), `UserControllerTest` (E2E)

### 관련 엔티티

- `User` (@Entity: "users") - fields: id, email, password, nickname, providerType, deletedAt

### DB 마이그레이션

- V1: 초기 스키마
- V2: `V2__alter_users__mysql.sql`
- V6: `V6__alter_users__mysql.sql`

---

## 2-3. moment.md (PREFIX: MOM, 4 features, ~80줄)

### 기능 목록

| ID | 기능 | API | Key Classes |
|----|------|-----|-------------|
| MOM-001 | 기본 모멘트 생성 | `POST /api/v2/moments` | MomentController, MomentCreateFacadeService, MomentApplicationService, MomentService |
| MOM-002 | 추가 모멘트 생성 | `POST /api/v2/moments/extra` | MomentController, MomentCreateFacadeService, MomentApplicationService |
| MOM-003 | 기본 작성 가능 여부 | `GET /api/v2/moments/writable/basic` | MomentController, MomentApplicationService |
| MOM-004 | 추가 작성 가능 여부 | `GET /api/v2/moments/writable/extra` | MomentController, MomentApplicationService |

### 비즈니스 규칙

- 기본 모멘트: 하루 1회 (`OnceADayPolicy`)
- 추가 모멘트: 포인트 소모 (`PointDeductionPolicy`)
- 모멘트 내용: 1~200자
- 작성 유형: `BASIC` 또는 `EXTRA`

### 관련 에러 코드

- M-001: 내용 비어있음 (400)
- M-002: 존재하지 않는 모멘트 (404)
- M-004: 글자수 초과 (400)
- M-005: 유효하지 않은 페이지 사이즈 (400)

### 관련 테스트 클래스 (9개)

- `MomentTest`, `MomentGroupContextTest`
- `MomentRepositoryTest`, `MomentImageRepositoryTest`
- `MomentApplicationServiceTest`, `MomentServiceTest`, `MomentImageServiceTest`
- `MyGroupMomentPageFacadeServiceTest`
- `MomentControllerTest` (E2E)

### 관련 엔티티

- `Moment` (@Entity: "moments") - implements Cursorable
- `MomentImage` (@Entity: "moment_images")

### DB 마이그레이션

- V1: 초기 스키마
- V3, V5: `V3/V5__alter_moments__mysql.sql`
- V13~V15: 이미지 테이블
- V21: `V21__create_moments_index__mysql.sql`
- V30: `V30__alter_moments_for_groups.sql`

---

## 2-4. comment.md (PREFIX: CMT, 1 feature, ~50줄)

### 기능 목록

| ID | 기능 | API | Key Classes |
|----|------|-----|-------------|
| CMT-001 | 댓글/Echo 생성 | `POST /api/v2/comments` | CommentController, CommentCreateFacadeService, CommentApplicationService, CommentService |

### 이벤트 발행

- `CommentCreateEvent` → NotificationEventHandler

### 관련 에러 코드

- C-001 ~ C-007: 댓글 관련 에러

### 관련 테스트 클래스 (9개)

- `CommentTest`, `CommentMemberContextTest`
- `CommentRepositoryTest`, `CommentImageRepositoryTest`
- `CommentApplicationServiceTest`, `CommentServiceTest`, `CommentImageServiceTest`
- `MyGroupCommentPageFacadeServiceTest`
- `CommentControllerTest` (E2E)

### 관련 엔티티

- `Comment` (@Entity: "comments") - implements Cursorable
- `CommentImage` (@Entity: "comment_images")

### DB 마이그레이션

- V4: `V4__alter_comments__mysql.sql`
- V18, V19: `V18/V19__create_comments__mysql.sql`
- V20: `V20__create_comments_index__mysql.sql`
- V31: `V31__alter_comments_for_groups.sql`

---

## 2-5. group.md (PREFIX: GRP, 29 features, ~350줄)

### 컨트롤러 매핑

| Controller | Base Path | 기능 수 |
|------------|-----------|---------|
| `GroupController` | `/api/v2/groups` | 5 (CRUD + 목록) |
| `GroupMemberController` | `/api/v2/groups/{groupId}` | 4 (멤버 목록, 대기 목록, 프로필, 탈퇴) |
| `GroupMemberApprovalController` | `/api/v2/groups/{groupId}` | 4 (강퇴, 승인, 거절, 소유권이전) |
| `GroupInviteController` | `/api/v2` | 3 (초대 생성, 조회, 가입) |
| `GroupMomentController` | `/api/v2/groups/{groupId}` | 7 (모멘트 CRUD + 좋아요 + 피드) |
| `GroupCommentController` | `/api/v2/groups/{groupId}` | 6 (댓글 CRUD + 좋아요 + 피드) |

### 기능 목록 (29개)

**그룹 관리 (GRP-001 ~ GRP-005)**:
- GRP-001 ~ GRP-005: 그룹 CRUD

**멤버 관리 (GRP-006 ~ GRP-013)**:
- GRP-006 ~ GRP-013: 멤버 목록, 대기, 프로필, 탈퇴, 강퇴, 승인, 거절, 소유권이전

**초대 (GRP-014 ~ GRP-016)**:
- GRP-014 ~ GRP-016: 초대 링크 생성, 조회, 코드 가입

**그룹 모멘트 (GRP-017 ~ GRP-023)**:
- GRP-017 ~ GRP-023: 모멘트 생성, 목록, 내 모멘트, 안읽음, 삭제, 좋아요, 댓글 가능

**그룹 댓글 (GRP-024 ~ GRP-029)**:
- GRP-024 ~ GRP-029: 댓글 생성, 목록, 삭제, 좋아요, 내 댓글, 안읽음

### 이벤트 발행

- `GroupJoinRequestEvent` → NotificationEventHandler
- `GroupJoinApprovedEvent` → NotificationEventHandler
- `GroupKickedEvent` → NotificationEventHandler
- `GroupCommentCreateEvent` → NotificationEventHandler

### 관련 에러 코드

- GR-001 ~ GR-003: 그룹 관련
- GM-001 ~ GM-008: 멤버 관련
- IL-001 ~ IL-002: 초대 링크 관련

### 관련 테스트 클래스 (18개)

**Domain**: `GroupTest`, `GroupMemberTest`, `GroupInviteLinkTest`, `MemberRoleTest`, `MemberStatusTest`
**Service**: `GroupServiceTest`, `GroupMemberServiceTest`, `InviteLinkServiceTest`, `GroupApplicationServiceTest`, `GroupMemberApplicationServiceTest`
**E2E**: `GroupControllerTest`, `GroupMemberControllerTest`, `GroupInviteControllerTest`, `GroupMomentControllerTest`, `GroupCommentControllerTest`

### 관련 엔티티

- `Group` (@Entity: "moment_groups")
- `GroupMember` (@Entity: "group_members") - MemberRole, MemberStatus enums
- `GroupInviteLink` (@Entity: "group_invite_links")

### DB 마이그레이션

- V27: `V27__create_groups.sql`
- V28: `V28__create_group_members.sql`
- V29: `V29__create_group_invite_links.sql`
- V30: `V30__alter_moments_for_groups.sql`
- V31: `V31__alter_comments_for_groups.sql`
- V32: `V32__create_likes.sql`
- V33: `V33__alter_notifications_for_groups.sql`
- V34: `V34__rename_groups_to_moment_groups.sql`

---

## 2-6. like.md (PREFIX: LIK, 2 features, ~50줄)

### 기능 목록

| ID | 기능 | API | Key Classes |
|----|------|-----|-------------|
| LIK-001 | 모멘트 좋아요 토글 | `POST /api/v2/groups/{groupId}/moments/{momentId}/like` | GroupMomentController, MomentLikeService |
| LIK-002 | 댓글 좋아요 토글 | `POST /api/v2/groups/{groupId}/comments/{commentId}/like` | GroupCommentController, CommentLikeService |

### 이벤트 발행

- `MomentLikeEvent` → NotificationEventHandler
- `CommentLikeEvent` → NotificationEventHandler

### 관련 테스트 클래스 (4개)

- `MomentLikeTest`, `CommentLikeTest`
- `MomentLikeServiceTest`, `CommentLikeServiceTest`

### 관련 엔티티

- `MomentLike` (@Entity: "moment_likes") - UniqueConstraint(moment_id, member_id)
- `CommentLike` (@Entity: "comment_likes") - UniqueConstraint(comment_id, member_id)

### DB 마이그레이션

- V32: `V32__create_likes.sql`

---

## 2-7. notification.md (PREFIX: NTF, 6 features, ~100줄)

### 기능 목록

| ID | 기능 | API | Key Classes |
|----|------|-----|-------------|
| NTF-001 | SSE 구독 | `GET /api/v2/notifications/subscribe` | NotificationController, SseNotificationService |
| NTF-002 | 알림 목록 조회 | `GET /api/v2/notifications` | NotificationController, NotificationApplicationService |
| NTF-003 | 단건 읽음 처리 | `PATCH /api/v2/notifications/{id}/read` | NotificationController, NotificationService |
| NTF-004 | 전체 읽음 처리 | `PATCH /api/v2/notifications/read-all` | NotificationController, NotificationService |
| NTF-005 | 디바이스 등록 | `POST /api/v2/push-notifications` | PushNotificationController, PushNotificationApplicationService |
| NTF-006 | 디바이스 해제 | `DELETE /api/v2/push-notifications` | PushNotificationController, PushNotificationService |

### 핵심 아키텍처

- `NotificationFacadeService`: SSE + Push + DB 조율 (메인 진입점)
- `NotificationEventHandler`: 8개 도메인 이벤트 구독 (모든 알림의 트리거)
- `@Async` + `@TransactionalEventListener(AFTER_COMMIT)` 패턴

### 관련 테스트 클래스 (15개)

- Domain: `NotificationTest`, `NotificationTypeTest`
- Infrastructure: `EmittersTest`, `FcmPushNotificationSenderTest`, `NotificationRepositoryTest`, `PushNotificationRepositoryTest`
- Service: `NotificationApplicationServiceTest`, `NotificationEventHandlerTest`, `NotificationFacadeServiceTest`, `NotificationServiceTest`, `PushNotificationServiceTest`, `SseNotificationServiceTest`
- DTO: `NotificationResponseTest`
- E2E: `NotificationControllerTest`, `PushNotificationControllerTest`

### 관련 엔티티

- `Notification` (@Entity: "notifications") - NotificationType, TargetType enums
- `PushNotification` (@Entity: "push_notifications")

### DB 마이그레이션

- V22: `V22__create_pushNotification__mysql.sql`
- V33: `V33__alter_notifications_for_groups.sql`

---

## 2-8. report.md (PREFIX: RPT, 2 features, ~60줄)

### 기능 목록

| ID | 기능 | API | Key Classes |
|----|------|-----|-------------|
| RPT-001 | 모멘트 신고 | `POST /api/v2/moments/{id}/reports` | ReportController, ReportCreateFacadeService, ReportApplicationService |
| RPT-002 | 댓글 신고 | `POST /api/v2/comments/{id}/reports` | ReportController, ReportCreateFacadeService, ReportApplicationService |

### 신고 사유 (ReportReason enum)

- SPAM_OR_ADVERTISEMENT
- SEXUAL_CONTENT
- HATE_SPEECH_OR_DISCRIMINATION
- ABUSE_OR_HARASSMENT
- VIOLENT_OR_DANGEROUS_CONTENT
- PRIVACY_VIOLATION
- ILLEGAL_INFORMATION

### 관련 테스트 클래스 (2개)

- `ReportServiceTest`, `ReportRepositoryTest`

### 관련 엔티티

- `Report` (@Entity: "reports") - ReportReason enum

### DB 마이그레이션

- V16: `V16__create_reports__mysql.sql`

---

## 2-9. storage.md (PREFIX: STG, 1 feature, ~40줄)

### 기능 목록

| ID | 기능 | API | Key Classes |
|----|------|-----|-------------|
| STG-001 | 업로드 URL 발급 | `POST /api/v2/storage/upload-url` | FileStorageController, FileStorageService |

### 관련 테스트 클래스 (3개)

- `FileStorageServiceTest`, `PhotoUrlResolverTest`, `AwsS3ClientTest`

### 관련 엔티티

- 없음 (Presigned URL 방식, 엔티티 불필요)

---

## 2-10. admin.md (PREFIX: ADM, 34 features, ~400줄)

### 컨트롤러 매핑

| Controller | Base Path | 기능 수 |
|------------|-----------|---------|
| `AdminAuthApiController` | `/api/admin/auth` | 3 (로그인, 로그아웃, 현재관리자) |
| `AdminUserApiController` | `/api/admin/users` | 4 (목록, 상세, 수정, 삭제) |
| `AdminGroupApiController` | `/api/admin/groups` | 19 (통계, 그룹CRUD, 멤버, 콘텐츠 등) |
| `AdminAccountApiController` | `/api/admin/accounts` | 4 (목록, 생성, 차단, 해제) |
| `AdminSessionApiController` | `/api/admin/sessions` | 5 (목록, 상세, 무효화, 관리자전체무효화, 이력) |

### AdminGroupApiController 상세 엔드포인트 (19개)

| 엔드포인트 | Feature ID |
|-----------|------------|
| `GET /stats` | ADM-008 |
| `GET /` | ADM-009 |
| `GET /{groupId}` | ADM-010 |
| `GET /{groupId}/members` | ADM-011 |
| `GET /{groupId}/pending-members` | ADM-012 |
| `PUT /{groupId}` | ADM-013 |
| `DELETE /{groupId}` | ADM-014 |
| `POST /{groupId}/restore` | ADM-015 |
| `POST /{groupId}/members/{memberId}/approve` | ADM-016 |
| `POST /{groupId}/members/{memberId}/reject` | ADM-017 |
| `DELETE /{groupId}/members/{memberId}` | ADM-018 |
| `POST /{groupId}/transfer-ownership/{id}` | ADM-019 |
| `GET /{groupId}/invite-link` | ADM-020 |
| `GET /logs` | ADM-021 |
| `GET /{groupId}/moments` | ADM-022 |
| `DELETE /{groupId}/moments/{momentId}` | ADM-023 |
| `GET /{groupId}/moments/{momentId}/comments` | ADM-024 |
| `DELETE /{groupId}/comments/{commentId}` | ADM-025 |

### 에러 코드 (AdminErrorCode)

- A-001 ~ A-011: 관리자 인증/권한
- AG-001 ~ AG-003: 그룹 관련
- AM-001 ~ AM-007: 멤버 관련
- AC-001 ~ AC-004: 콘텐츠 관련
- A-500: 서버 내부 오류

### 인증 방식

세션 기반: `AdminAuthInterceptor` + `AdminSessionManager`

### 관련 테스트 클래스 (29개)

**Service**: `AdminServiceTest`, `AdminUserServiceTest`, `AdminGroupServiceTest`, `AdminGroupMemberServiceTest`, `AdminGroupLogServiceTest`, `AdminContentServiceTest`, `AdminSessionServiceTest`, `AdminSessionManagerTest`, `ClientIpExtractorTest`, `UserAgentParserTest`
**Domain**: `AdminTest`, `AdminGroupLogTest`
**E2E** (17개): `AdminGroupListApiTest`, `AdminGroupDetailApiTest`, `AdminGroupStatsApiTest`, `AdminGroupDeleteApiTest`, `AdminGroupRestoreApiTest`, `AdminGroupUpdateApiTest`, `AdminGroupMemberApiTest`, `AdminMemberApproveApiTest`, `AdminMemberRejectApiTest`, `AdminMemberKickApiTest`, `AdminOwnershipTransferApiTest`, `AdminInviteLinkApiTest`, `AdminGroupLogApiTest`, `AdminMomentListApiTest`, `AdminMomentDeleteApiTest`, `AdminCommentListApiTest`, `AdminCommentDeleteApiTest`

### 관련 엔티티

- `Admin` (@Entity: "admins") - AdminRole enum
- `AdminSession` (@Entity: "admin_sessions")
- `AdminGroupLog` (@Entity: "admin_group_logs") - AdminGroupLogType enum

### DB 마이그레이션

- V23: `V23__create_admin_table__mysql.sql`
- V24: `V24__create_admin_sessions.sql`
- V35: `V35__create_admin_group_logs.sql`

---

## 2-11. global.md (PREFIX: GLB, 7 features, ~80줄)

### 기능 목록

| ID | 기능 | Key Classes |
|----|------|-------------|
| GLB-001 | BaseEntity (감사 필드) | `BaseEntity` - createdAt, updatedAt |
| GLB-002 | ErrorCode + MomentException | `ErrorCode`, `MomentException`, `GlobalExceptionHandler` |
| GLB-003 | Cursor 기반 페이지네이션 | `Cursor`, `PageSize`, `Cursorable` |
| GLB-004 | Soft Delete 패턴 | `@SQLDelete`, `@SQLRestriction` |
| GLB-005 | Logstash 구조화 로깅 | `ControllerLogAspect` |
| GLB-006 | SuccessResponse/ErrorResponse | `SuccessResponse`, `ErrorResponse` |
| GLB-007 | Health Check | `HealthCheckController` → `GET /health` |

### 관련 테스트 클래스 (1개)

- `TargetTypeTest`

---

## 작업 순서

1. `.claude/docs/features/` 디렉토리 확인/생성
2. 도메인 순서대로 파일 생성: auth → user → moment → comment → group → like → notification → report → storage → admin → global
3. 각 파일 생성 시 실제 소스 코드에서 클래스명, 메서드명 검증
4. 생성 후 Phase 1의 Quick Reference 기능 수와 대조

## 선행 조건

- Phase 1 완료 (FEATURES.md 틀 존재)

## 후행 조건

- Phase 1의 Quick Reference 기능 수 업데이트

## 검증 기준

- [ ] 11개 도메인 파일 모두 생성
- [ ] 각 파일의 Feature ID 연번이 올바른지 (AUTH-001~011, USER-001~007 등)
- [ ] API 엔드포인트가 실제 Controller 매핑과 일치하는지
- [ ] Key Classes가 실제 존재하는 클래스인지
- [ ] 테스트 클래스 목록이 실제 테스트 파일과 일치하는지
- [ ] 에러 코드가 ErrorCode/AdminErrorCode enum과 일치하는지