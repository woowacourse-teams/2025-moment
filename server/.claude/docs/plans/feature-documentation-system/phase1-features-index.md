# Phase 1: FEATURES.md (중앙 인덱스) 생성

> Status: PENDING
> Parent Plan: feature-documentation-system.md

## 목표

모든 도메인 기능을 한눈에 파악할 수 있는 중앙 인덱스 파일 생성

## 생성 파일

`.claude/docs/features/FEATURES.md` (신규, ~80줄)

## 상세 작업

### 1. 헤더 섹션

```markdown
# Moment Feature Registry
> Last Updated: 2026-02-03
```

### 2. Quick Reference 테이블

실제 코드베이스 분석 기반 정확한 기능 수:

| 도메인 | PREFIX | 기능 수 | 상태 | 상세 문서 |
|--------|--------|---------|------|-----------|
| auth | AUTH | 11 | DONE | [auth.md](auth.md) |
| user | USER | 7 | DONE | [user.md](user.md) |
| moment | MOM | 4 | DONE | [moment.md](moment.md) |
| comment | CMT | 1 | DONE | [comment.md](comment.md) |
| group | GRP | 29 | DONE | [group.md](group.md) |
| like | LIK | 2 | DONE | [like.md](like.md) |
| notification | NTF | 6 | DONE | [notification.md](notification.md) |
| report | RPT | 2 | DONE | [report.md](report.md) |
| storage | STG | 1 | DONE | [storage.md](storage.md) |
| admin | ADM | 34 | DONE | [admin.md](admin.md) |
| global | GLB | 7 | DONE | [global.md](global.md) |

**총 104개 기능**

### 3. Cross-Domain Dependencies (이벤트 기반)

코드베이스에서 확인된 8개 도메인 이벤트:

| Event | 발행 도메인 | 발행 클래스 | 구독 도메인 | 구독 클래스 |
|-------|------------|------------|------------|------------|
| `CommentCreateEvent` | comment | `CommentCreateFacadeService` | notification | `NotificationEventHandler` |
| `EchoCreateEvent` | comment | (레거시/대체명) | notification | `NotificationEventHandler` |
| `GroupCommentCreateEvent` | comment | Group comment facade | notification | `NotificationEventHandler` |
| `GroupJoinRequestEvent` | group | `GroupMemberApplicationService` | notification | `NotificationEventHandler` |
| `GroupJoinApprovedEvent` | group | Group member service | notification | `NotificationEventHandler` |
| `GroupKickedEvent` | group | Group member service | notification | `NotificationEventHandler` |
| `MomentLikeEvent` | like | `MomentLikeService` | notification | `NotificationEventHandler` |
| `CommentLikeEvent` | like | `CommentLikeService` | notification | `NotificationEventHandler` |

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

### 4. Recent Changes 섹션

```markdown
## Recent Changes (최근 20건)

| 날짜 | 도메인 | Feature ID | 변경 내용 |
|------|--------|-----------|-----------|
| 2026-02-03 | all | - | 초기 Feature Registry 생성 |
```

## 작업 순서

1. `.claude/docs/features/` 디렉토리 생성
2. FEATURES.md 헤더 작성
3. Quick Reference 테이블 작성 (Phase 2 완료 후 기능 수 재검증)
4. Cross-Domain Dependencies 작성
5. Recent Changes 초기화

## 선행 조건

- 없음 (독립 작업)

## 후행 조건

- Phase 2에서 도메인별 문서 생성 시 기능 수가 변경되면 Quick Reference 업데이트 필요

## 검증 기준

- [ ] Quick Reference의 기능 수 합계 = 104
- [ ] 8개 도메인 이벤트가 모두 Cross-Domain Dependencies에 기록
- [ ] 상대 경로 링크가 올바른지 확인