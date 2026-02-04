# Moment Feature Registry

> Last Updated: 2026-02-04

## Quick Reference

| 도메인 | PREFIX | 기능 수 | 상태 | 상세 문서 |
|--------|--------|---------|------|-----------|
| auth | AUTH | 12 | DONE | [auth.md](auth.md) |
| user | USER | 8 | DONE | [user.md](user.md) |
| moment | MOM | 4 | DONE | [moment.md](moment.md) |
| comment | CMT | 1 | DONE | [comment.md](comment.md) |
| group | GRP | 29 | DONE | [group.md](group.md) |
| like | LIK | 2 | DONE | [like.md](like.md) |
| notification | NTF | 6 | DONE | [notification.md](notification.md) |
| report | RPT | 2 | DONE | [report.md](report.md) |
| storage | STG | 1 | DONE | [storage.md](storage.md) |
| admin | ADM | 34 | DONE | [admin.md](admin.md) |
| global | GLB | 7 | DONE | [global.md](global.md) |

**총 106개 기능**

## Cross-Domain Dependencies (이벤트 기반)

| Event | 발행 도메인 | 발행 클래스 | 구독 도메인 | 구독 클래스 | 비고 |
|-------|------------|------------|------------|------------|------|
| `CommentCreateEvent` | comment | `CommentCreateFacadeService` | notification | `NotificationEventHandler` | ✅ 활성 |
| `EchoCreateEvent` | comment | - | - | - | ⚠️ 미사용 dead code (발행/구독 없음) |
| `GroupCommentCreateEvent` | comment | - | notification | `NotificationEventHandler` | ⚠️ handler만 존재, 미발행 |
| `GroupJoinRequestEvent` | group | `GroupMemberApplicationService` | notification | `NotificationEventHandler` | ✅ 활성 |
| `GroupJoinApprovedEvent` | group | `GroupMemberApplicationService` | notification | `NotificationEventHandler` | ✅ 활성 |
| `GroupKickedEvent` | group | `GroupMemberApplicationService` | notification | `NotificationEventHandler` | ✅ 활성 |
| `MomentLikeEvent` | like | `MomentLikeService` | notification | `NotificationEventHandler` | ✅ 활성 |
| `CommentLikeEvent` | like | `CommentLikeService` | notification | `NotificationEventHandler` | ✅ 활성 |

**이벤트 파일 위치**:

- `moment/comment/dto/CommentCreateEvent.java`
- `moment/comment/dto/EchoCreateEvent.java`
- `moment/comment/dto/event/GroupCommentCreateEvent.java`
- `moment/group/dto/event/GroupJoinRequestEvent.java`
- `moment/group/dto/event/GroupJoinApprovedEvent.java`
- `moment/group/dto/event/GroupKickedEvent.java`
- `moment/like/dto/event/MomentLikeEvent.java`
- `moment/like/dto/event/CommentLikeEvent.java`

**구독자**: 단일 클래스 `NotificationEventHandler`가 모든 이벤트 처리

## Recent Changes (최근 20건)

| 날짜 | 도메인 | Feature ID | 변경 내용 |
|------|--------|-----------|-----------|
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
