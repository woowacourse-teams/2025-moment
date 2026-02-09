# Comment Domain

**Last Updated:** 2026-02-10

## Key Classes

| Layer | Class | Path |
|-------|-------|------|
| Entity | `Comment` | `moment/comment/domain/Comment.java` |
| Entity | `CommentImage` | `moment/comment/domain/CommentImage.java` |
| Value Object | `CommentCreationStatus` | `moment/comment/domain/CommentCreationStatus.java` |
| Controller | `CommentController` | `moment/comment/presentation/CommentController.java` |
| Facade | `CommentCreateFacadeService` | `moment/comment/service/facade/CommentCreateFacadeService.java` |
| Facade | `GroupCommentCreateFacadeService` | `moment/comment/service/facade/GroupCommentCreateFacadeService.java` |
| Facade | `MyGroupCommentPageFacadeService` | `moment/comment/service/facade/MyGroupCommentPageFacadeService.java` |
| Application | `CommentApplicationService` | `moment/comment/service/application/CommentApplicationService.java` |
| Domain Service | `CommentService` | `moment/comment/service/comment/CommentService.java` |
| Domain Service | `CommentImageService` | `moment/comment/service/comment/CommentImageService.java` |
| Repository | `CommentRepository` | `moment/comment/infrastructure/CommentRepository.java` |
| Repository | `CommentImageRepository` | `moment/comment/infrastructure/CommentImageRepository.java` |
| DTO | `CommentComposition` | `moment/comment/dto/tobe/CommentComposition.java` |
| DTO | `CommentCompositions` | `moment/comment/dto/tobe/CommentCompositions.java` |
| Interface | `CommentComposable` | `moment/comment/service/application/CommentComposable.java` |
| Event | `CommentCreateEvent` | `moment/comment/dto/CommentCreateEvent.java` |
| Event | `GroupCommentCreateEvent` | `moment/comment/dto/event/GroupCommentCreateEvent.java` |

## Features

### CMT-001: Comment Creation (Personal)

- **Status:** `DONE`
- **Endpoint:** `POST /api/v2/comments`
- **Service Flow:** `CommentController` -> `CommentCreateFacadeService` -> `CommentApplicationService` -> `CommentService`
- **Business Rules:** 1 comment per user per moment, content 1~200 characters
- **Event Published:** `CommentCreateEvent` (skipped if commenter == momenter)
- **Error Codes:** `C-001` ~ `C-007`, `T-005`, `U-002`, `M-001`, `BL-004`

### CMT-002: Comment Creation (Group)

- **Status:** `DONE`
- **Service Flow:** `GroupCommentCreateFacadeService` -> `CommentApplicationService.createCommentInGroup()` -> `CommentService.createWithMember()`
- **Event Published:** `GroupCommentCreateEvent` (skipped if commenter == momenter)
- **Dependencies:** `MomentApplicationService`, `UserBlockApplicationService`

### CMT-003: My Comment Compositions (Cursor Pagination)

- **Status:** `DONE`
- **Service Flow:** `CommentApplicationService.getMyCommentCompositions()` -> `CommentService.getCommentsBy()`
- **Pagination:** Cursor-based (`createdAt_id`), two-step query (IDs first, then entities)

### CMT-004: Unread Comment Compositions

- **Status:** `DONE`
- **Service Flow:** `CommentApplicationService.getUnreadMyCommentCompositions()` -> `CommentService.getCommentsBy(commentIds)`

### CMT-005: Comments in Group

- **Status:** `DONE`
- **Service Flow:** `CommentApplicationService.getCommentsInGroup()` -> `CommentService.getAllByMomentIds()`
- **Response DTO:** `List<GroupCommentResponse>`
- **Dependencies:** `GroupMemberService`, `CommentLikeService`, `UserBlockApplicationService`

### CMT-006: Delete Comment in Group

- **Status:** `DONE`
- **Service Flow:** `CommentApplicationService.deleteCommentInGroup()` -> `CommentService.deleteBy()`
- **Business Rules:** Only the comment author (member) can delete

### CMT-007: Comment Compositions for Moment Page

- **Status:** `DONE`
- **Service Flow:** `CommentApplicationService.getMyCommentCompositionsBy(momentIds)` -> `CommentService.getAllByMomentIds()`
- **Response DTO:** `List<CommentComposition>` (id, content, nickname, imageUrl, commentCreatedAt, momentId, commenterUserId, memberId)
- **Recent Change (2026-02-10):** `memberId` 필드 추가. `CommentRepository.findAllWithMemberAndCommenterByMomentIdIn` 쿼리로 member와 commenter를 LEFT JOIN FETCH로 로드. 기존 `findAllByMomentIdIn` 제거. `CommentService.getAllByMomentIds()`에서 사용. N+1 Lazy Loading 방지.

### CMT-008: My Group Moment Comment Response

- **Status:** `DONE`
- **Response DTO:** `MyGroupMomentCommentResponse` (id, memberId, content, memberNickname, imageUrl, createdAt, likeCount, hasLiked)
- **Used by:** `MyGroupMomentPageFacadeService` (moment domain)
- **Recent Change (2026-02-10):** `memberId` 필드 추가. `CommentComposition.memberId()`에서 매핑.

### CMT-009: My Group Comment Page

- **Status:** `DONE`
- **Service Flow:** `MyGroupCommentPageFacadeService` -> `CommentService`, `CommentApplicationService`, `MomentApplicationService`
- **Response DTO:** `MyGroupCommentListResponse` -> `MyGroupCommentResponse`
- **Pagination:** Cursor-based (ID), default page size 10
- **Dependencies:** `GroupMemberService`, `NotificationApplicationService`, `CommentLikeService`, `MomentLikeService`, `UserBlockApplicationService`
- **Recent Change (2026-02-10):** 직접 `CommentComposition` 생성자 호출에 8번째 인자 `memberId` 추가. fallback 경로에서 `comment.getMember().getId()`로 매핑.

## Domain Events Published

| Event | Published By | Fields | Trigger |
|-------|-------------|--------|---------|
| `CommentCreateEvent` | `CommentCreateFacadeService` | momentId, momenterId, commenterId, groupId | Comment creation (personal), skipped if self-comment |
| `GroupCommentCreateEvent` | `GroupCommentCreateFacadeService` | groupId, momentId, momentOwnerId, commentId, commenterId, commenterNickname | Comment creation (group), skipped if self-comment |

## Business Rules

- Comment content: 1~200 characters, non-null, non-blank
- Commenter (User) is required
- momentId is required, non-null
- 1 comment per user per moment (unique constraint)
- Comment delete threshold: 1 report
- Soft Delete: `@SQLDelete` + `@SQLRestriction("deleted_at IS NULL")`

## Dependencies

| Depends On | Usage |
|------------|-------|
| `user` | `UserService` - commenter lookup |
| `moment` | `MomentService`, `MomentApplicationService` - moment lookup, composition |
| `group` | `GroupMemberService` - group membership verification |
| `like` | `CommentLikeService` - like count and toggle |
| `notification` | `NotificationApplicationService` - unread comment IDs |
| `storage` | `PhotoUrlResolver` - image URL resolution |
| `block` | `UserBlockApplicationService` - blocked user filtering |

## Error Codes

| Code | Message | HTTP Status |
|------|---------|-------------|
| `C-001` | 유효하지 않은 코멘트입니다. | 400 |
| `C-002` | 존재하지 않는 코멘트입니다. | 404 |
| `C-003` | 모멘트에 등록된 코멘트가 이미 존재합니다. | 409 |
| `C-004` | 유효하지 않은 코멘트 형식입니다. | 400 |
| `C-005` | 유효하지 않은 코멘트 ID입니다. | 400 |
| `C-006` | 유효하지 않은 페이지 사이즈입니다. | 400 |
| `C-007` | 코멘트는 1자 이상, 200자 이하로만 작성 가능합니다. | 400 |

## DB Migrations

| Version | Description |
|---------|-------------|
| V4 | Alter comments |
| V18 | Create comments |
| V19 | Create comments |
| V20 | Create comments index |
| V31 | Alter comments for groups (member_id) |

## Tests

- `CommentCompositionTest` - CommentComposition DTO mapping (memberId null 매핑 테스트 포함)
