# Moment Domain

**Last Updated:** 2026-02-09

## Key Classes

| Layer | Class | Path |
|-------|-------|------|
| Entity | `Moment` | `moment/moment/domain/Moment.java` |
| Entity | `MomentImage` | `moment/moment/domain/MomentImage.java` |
| Value Object | `MomentCreationStatus` | `moment/moment/domain/MomentCreationStatus.java` |
| Controller | `MomentController` | `moment/moment/presentation/MomentController.java` |
| Facade | `MomentCreateFacadeService` | `moment/moment/service/facade/MomentCreateFacadeService.java` |
| Facade | `CommentableMomentFacadeService` | `moment/moment/service/facade/CommentableMomentFacadeService.java` |
| Facade | `MyGroupMomentPageFacadeService` | `moment/moment/service/facade/MyGroupMomentPageFacadeService.java` |
| Application | `MomentApplicationService` | `moment/moment/service/application/MomentApplicationService.java` |
| Domain Service | `MomentService` | `moment/moment/service/moment/MomentService.java` |
| Domain Service | `MomentImageService` | `moment/moment/service/moment/MomentImageService.java` |
| Repository | `MomentRepository` | `moment/moment/infrastructure/MomentRepository.java` |
| Repository | `MomentImageRepository` | `moment/moment/infrastructure/MomentImageRepository.java` |

## Features

### MOM-001: Basic Moment Creation

- **Status:** `DONE`
- **Endpoint:** `POST /api/v2/moments`
- **Service Flow:** `MomentController` -> `MomentCreateFacadeService` -> `MomentApplicationService` -> `MomentService`
- **Business Rules:** 기본 모멘트 생성 (content 1~200자, momenter 필수)
- **Error Codes:** `T-005`, `U-002`

### MOM-002: Extra Moment Creation

- **Status:** `DONE`
- **Endpoint:** `POST /api/v2/moments/extra`
- **Service Flow:** `MomentController` -> `MomentCreateFacadeService` -> `MomentApplicationService` -> `MomentService`
- **Business Rules:** 추가 모멘트 생성
- **Error Codes:** `T-005`, `U-002`, `M-006`

### MOM-003: Basic Moment Creation Status Check

- **Status:** `DONE`
- **Endpoint:** `GET /api/v2/moments/writable/basic`
- **Service Flow:** `MomentController` -> `MomentApplicationService` -> `UserService`

### MOM-004: Extra Moment Creation Status Check

- **Status:** `DONE`
- **Endpoint:** `GET /api/v2/moments/writable/extra`
- **Service Flow:** `MomentController` -> `MomentApplicationService` -> `UserService`

### MOM-005: My Moment Compositions (Cursor Pagination)

- **Status:** `DONE`
- **Service Flow:** `MomentApplicationService.getMyMomentCompositions()` -> `MomentService.getMomentsBy()`
- **Pagination:** Cursor-based (`createdAt_id`)

### MOM-006: Unread Moment Compositions

- **Status:** `DONE`
- **Service Flow:** `MomentApplicationService.getUnreadMyMomentCompositions()` -> `MomentService.getUnreadMomentsBy()`

### MOM-007: Group Moment List

- **Status:** `DONE`
- **Service Flow:** `MomentApplicationService.getGroupMoments()` -> `MomentService.getByGroup()`
- **Pagination:** Cursor-based (ID)
- **Dependencies:** `GroupMemberService`, `MomentLikeService`, `CommentService`, `UserBlockApplicationService`

### MOM-008: Commentable Moment in Group

- **Status:** `DONE`
- **Service Flow:** `CommentableMomentFacadeService` -> `MomentApplicationService.getCommentableMomentIdsInGroup()` -> `MomentApplicationService.pickRandomMomentComposition()`
- **Business Rules:** 7일 이내 모멘트 중 내가 작성하지 않은 것, 신고하지 않은 것, 차단한 유저가 아닌 것에서 랜덤 선택 + 아직 코멘트를 작성하지 않은 모멘트만 필터
- **Response DTO:** `CommentableMomentResponse` (id, memberId, nickname, content, imageUrl, createdAt)
- **Recent Change (2026-02-09):** `memberId` 필드 추가. `MomentRepository.findAllWithMomenterByIds` 쿼리에 `LEFT JOIN FETCH m.member` 추가하여 Lazy Loading 방지.

### MOM-009: My Group Moment Page (with Comments)

- **Status:** `DONE`
- **Service Flow:** `MyGroupMomentPageFacadeService` -> `MomentService`, `CommentApplicationService`, `MomentLikeService`, `CommentLikeService`
- **Response DTO:** `MyGroupMomentListResponse` -> `MyGroupMomentResponse` -> `MyGroupMomentCommentResponse`
- **Dependencies:** `GroupMemberService`, `NotificationApplicationService`, `MomentImageService`, `UserBlockApplicationService`

### MOM-010: Group Moment CRUD

- **Status:** `DONE`
- **Service Flow:** `MomentApplicationService.createMomentInGroup()`, `deleteMomentInGroup()`, `toggleMomentLike()`
- **Dependencies:** `GroupMemberService`, `MomentLikeService`, `UserBlockApplicationService`

## Business Rules

- Moment content: 1~200 characters, non-null, non-empty
- Momenter (User) is required
- Commentable period: 7 days
- Moment delete threshold: 3 reports
- Soft Delete: `@SQLDelete` + `@SQLRestriction("deleted_at IS NULL")`

## Dependencies

| Depends On | Usage |
|------------|-------|
| `user` | `UserService` - momenter lookup |
| `group` | `GroupMemberService` - group membership verification |
| `comment` | `CommentService`, `CommentApplicationService` - comment count, compositions |
| `like` | `MomentLikeService` - like count and toggle |
| `report` | `ReportService` - reported moment filtering |
| `notification` | `NotificationApplicationService` - unread moment IDs |
| `storage` | `PhotoUrlResolver` - image URL resolution |
| `block` | `UserBlockApplicationService` - blocked user filtering |

## Error Codes

| Code | Message | HTTP Status |
|------|---------|-------------|
| `M-001` | 모멘트 내용이 비어있습니다. | 400 |
| `M-002` | 존재하지 않는 모멘트입니다. | 404 |
| `M-004` | 모멘트는 1자 이상, 100자 이하로만 작성 가능합니다. | 400 |
| `M-005` | 유효하지 않은 페이지 사이즈입니다. | 400 |

## DB Migrations

| Version | Description |
|---------|-------------|
| V1 | Initial schema (moments table) |
| V3 | Alter moments |
| V5 | Alter moments |
| V12 | Create moment_tags |
| V13 | Create images |
| V21 | Create moments index |
| V30 | Alter moments for groups (group_id, member_id) |

## Tests

- `CommentableMomentFacadeServiceTest` - Commentable moment selection in group
- `MomentCompositionTest` - Moment composition DTO mapping
