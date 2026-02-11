# Moment Feature Registry

> Last Updated: 2026-02-11

## Quick Reference

| 도메인 | PREFIX | 기능 수 | 상태 | 상세 문서 |
|--------|--------|---------|------|-----------|
| admin | ADM | 34 | DONE | [admin.md](admin.md) |
| auth | AUTH | 12 | DONE | [auth.md](auth.md) |
| block | BLK | 6 | DONE | [block.md](block.md) |
| comment | CMT | 1 | DONE | [comment.md](comment.md) |
| global | GLB | 7 | DONE | [global.md](global.md) |
| group | GRP | 29 | DONE | [group.md](group.md) |
| like | LIK | 2 | DONE | [like.md](like.md) |
| moment | MOM | 4 | DONE | [moment.md](moment.md) |
| notification | NTF | 6 | DONE | [notification.md](notification.md) |
| report | RPT | 2 | DONE | [report.md](report.md) |
| storage | STG | 1 | DONE | [storage.md](storage.md) |
| user | USER | 8 | DONE | [user.md](user.md) |

**총 112개 기능**

## Cross-Domain Dependencies (이벤트 기반)

| Event | 발행 도메인 | 발행 클래스 | 구독 도메인 | 구독 클래스 | 비고 |
|-------|------------|------------|------------|------------|------|
| `CommentCreateEvent` | comment | `CommentCreateFacadeService` | notification | `NotificationEventHandler` | ✅ 활성 |
| `GroupCommentCreateEvent` | comment | - | notification | `NotificationEventHandler` | ⚠️ handler만 존재, 미발행 |
| `GroupJoinRequestEvent` | group | `GroupMemberApplicationService` | notification | `NotificationEventHandler` | ✅ 활성 |
| `GroupJoinApprovedEvent` | group | `GroupMemberApplicationService` | notification | `NotificationEventHandler` | ✅ 활성 |
| `GroupKickedEvent` | group | `GroupMemberApplicationService` | notification | `NotificationEventHandler` | ✅ 활성 |
| `MomentLikeEvent` | like | `MomentLikeService` | notification | `NotificationEventHandler` | ✅ 활성 |
| `CommentLikeEvent` | like | `CommentLikeService` | notification | `NotificationEventHandler` | ✅ 활성 |

**이벤트 파일 위치** (api 모듈):

- `api/.../moment/comment/dto/CommentCreateEvent.java`
- `api/.../moment/comment/dto/event/GroupCommentCreateEvent.java`
- `api/.../moment/group/dto/event/GroupJoinRequestEvent.java`
- `api/.../moment/group/dto/event/GroupJoinApprovedEvent.java`
- `api/.../moment/group/dto/event/GroupKickedEvent.java`
- `api/.../moment/like/dto/event/MomentLikeEvent.java`
- `api/.../moment/like/dto/event/CommentLikeEvent.java`

**구독자**: 단일 클래스 `NotificationEventHandler`가 모든 이벤트 처리

## Cross-Domain Dependencies (서비스 직접 의존 - block)

| 소비 도메인 | 소비 클래스 | 제공 도메인 | 제공 클래스 | 용도 |
|-----------|-----------|-----------|-----------|------|
| comment | `CommentCreateFacadeService` | block | `UserBlockApplicationService` | 댓글 생성 시 차단 확인 |
| comment | `GroupCommentCreateFacadeService` | block | `UserBlockApplicationService` | 그룹 댓글 생성 시 차단 확인 |
| comment | `CommentApplicationService` | block | `UserBlockApplicationService` | 댓글 좋아요/목록 차단 필터링 |
| comment | `MyGroupCommentPageFacadeService` | block | `UserBlockApplicationService` | 그룹 댓글 페이지 차단 필터링 |
| moment | `MomentApplicationService` | block | `UserBlockApplicationService` | 모멘트 좋아요 차단, 피드 필터링 |
| moment | `MyGroupMomentPageFacadeService` | block | `UserBlockApplicationService` | 그룹 모멘트 피드 차단 필터링 |
| notification | `NotificationEventHandler` | block | `UserBlockApplicationService` | 차단된 사용자 알림 스킵 |

## Recent Changes (최근 20건)

| 날짜 | 도메인 | Feature ID | 변경 내용 |
|------|--------|-----------|-----------|
| 2026-02-11 | all | - | Gradle 멀티모듈 전환 (common/admin/api), EchoCreateEvent dead code 삭제 |
| 2026-02-11 | block | BLK-001~006 | UserBlockServiceTest, UserBlockApplicationServiceTest 통합 테스트 전환, findBlockedUserIds soft delete 테스트 추가 |
| 2026-02-10 | block | BLK-005 | existsBidirectionalBlock native query→JPQL 변경 (ClassCastException 해결), UserBlockRepositoryTest 추가 |
| 2026-02-09 | block | BLK-001~006 | 사용자 차단 기능 신규 추가 (차단/해제/목록 API, 피드 필터링, 상호작용 차단, 알림 필터링) |
| 2026-02-09 | moment, comment, like, notification | - | block 도메인 연동: 모멘트 피드 필터링, 댓글 필터링, 좋아요/댓글 생성 차단, 알림 필터링, Like 이벤트 likerUserId 추가 |
| 2026-02-06 | notification | NTF-001~006 | 딥링크 리팩토링: NotificationPayload 제거, SourceData(JSON)+DeepLinkGenerator 도입, userId 필터 추가, 복합 인덱스 V37 추가 |
| 2026-02-04 | user | USER-008 | 회원 탈퇴 API 구현 (DELETE /api/v2/me), UserWithdrawService 추가 |
| 2026-02-04 | moment, comment, like | - | 탈퇴 사용자 콘텐츠 표시 처리 (LEFT JOIN FETCH, null-safe 닉네임 표시, 좋아요 이벤트 null 체크) |
| 2026-02-04 | auth, user | AUTH-004, AUTH-012 | Apple 로그인 실제 이메일 사용, 레거시 이메일 마이그레이션, User.updateEmail() 추가 |
| 2026-02-04 | notification | NTF-003, NTF-004 | 서비스 레이어 참조 정정, RequestBody 파라미터 추가 |
| 2026-02-04 | comment | CMT-001 | Domain Events 테이블에 이벤트 상태 (dead code/미발행) 표기 |
| 2026-02-04 | group | GRP-010~013 | Admin E2E 테스트 참조 제거, User API E2E 테스트 미존재 명시 |
| 2026-02-04 | group | - | 테스트 수 18→15 정정 |
| 2026-02-04 | auth | AUTH-007~010 | AuthEmailService 레이어 분류 Domain→Application 수정 |
| 2026-02-04 | all | - | Cross-Domain Dependencies에 이벤트 활성 상태 표기, 발행 클래스 정정 |
| 2026-02-03 | all | - | 초기 Feature Registry 생성 |
