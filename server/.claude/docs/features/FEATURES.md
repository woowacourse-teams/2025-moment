# Feature Registry

**Last Updated:** 2026-02-09

## Quick Reference

| Domain | Features | Document |
|--------|----------|----------|
| moment | 10 | [moment.md](moment.md) |
| comment | 9 | [comment.md](comment.md) |

## Cross-Domain Dependencies

| Event | Publisher | Subscriber | Async |
|-------|-----------|------------|-------|
| `CommentCreateEvent` | `CommentCreateFacadeService` (comment) | `NotificationEventHandler` (notification) | Yes (AFTER_COMMIT) |
| `GroupCommentCreateEvent` | `GroupCommentCreateFacadeService` (comment) | `NotificationEventHandler` (notification) | Yes (AFTER_COMMIT) |

## Recent Changes

| Date | Domain | Feature ID | Change | Description |
|------|--------|------------|--------|-------------|
| 2026-02-09 | moment | MOM-008 | UPDATE | `CommentableMomentResponse`에 `memberId` 필드 추가, `findAllWithMomenterByIds` 쿼리에 `LEFT JOIN FETCH m.member` 추가 |
| 2026-02-09 | comment | CMT-007 | UPDATE | `CommentComposition`에 `memberId` 필드 추가, `findAllWithMemberByMomentIdIn` 쿼리 신규 추가 |
| 2026-02-09 | comment | CMT-008 | UPDATE | `MyGroupMomentCommentResponse`에 `memberId` 필드 추가 |
| 2026-02-09 | comment | CMT-009 | UPDATE | `MyGroupCommentPageFacadeService` 직접 생성자 호출에 `memberId` 인자 추가 |
