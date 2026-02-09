# Comment Domain (PREFIX: CMT)

> Last Updated: 2026-02-09
> Features: 1

## 탈퇴 사용자 콘텐츠 처리

- `CommentApplicationService.mapCommentsToUsers()`: commenter null-safe 처리 (HashMap으로 변환)
- `CommentComposition.of()`: commenter null 시 "탈퇴한 사용자" 표시

## 기능 목록

### CMT-001: 댓글/Echo 생성

- **Status**: DONE
- **API**: `POST /api/v2/comments`
- **Key Classes**:
    - Controller: `CommentController`
    - Facade: `CommentCreateFacadeService`
    - Application: `CommentApplicationService`
    - Domain: `CommentService`
    - Entity: `Comment`
    - DTO: `CommentCreateRequest`, `CommentCreateResponse`
- **Business Rules**: 모멘트에 댓글(Echo) 작성, 자신의 모멘트에도 작성 가능
- **Dependencies**: moment (MomentApplicationService)
- **Tests**: `CommentTest`, `CommentServiceTest`, `CommentApplicationServiceTest`, `CommentControllerTest` (E2E)
- **Error Codes**: C-001 ~ C-007

## Domain Events Published

| Event | 구독자 | 설명 | 상태 |
|-------|--------|------|------|
| `CommentCreateEvent` | `NotificationEventHandler` | 댓글 생성 시 모멘트 작성자에게 알림 | ✅ 활성 |
| `EchoCreateEvent` | - | record만 존재, 발행/구독 없음 | ⚠️ dead code |
| `GroupCommentCreateEvent` | `NotificationEventHandler` | 그룹 댓글 생성 시 알림 (handler만 존재, 미발행) | ⚠️ 미발행 |

## 차단 사용자 필터링

- `CommentCreateFacadeService`: 댓글 생성 시 차단된 사용자의 모멘트에 댓글 작성 불가 (`BLOCKED_USER_INTERACTION`)
- `GroupCommentCreateFacadeService`: 그룹 댓글 생성 시 차단 확인
- `CommentApplicationService.getGroupComments()`: 그룹 댓글 목록에서 차단된 사용자 댓글 필터링
- `CommentApplicationService.toggleCommentLike()`: 차단된 사용자 댓글 좋아요 시 `BLOCKED_USER_INTERACTION` 에러
- `CommentService.countByMomentIdExcludingBlocked()`: 차단된 사용자 댓글 제외 카운트
- `CommentRepository.countByMomentIdExcludingBlocked()`: NOT IN 조건 JPQL 쿼리 추가
- `CommentComposition`: `commenterUserId` 필드 추가 (차단 필터링용)
- `MyGroupCommentPageFacadeService`: 그룹 댓글 페이지에서 차단된 사용자 댓글 필터링

## 관련 에러 코드

- C-001 ~ C-007: 댓글 관련 에러

## 관련 엔티티

- `Comment` (@Entity: "comments") - implements Cursorable
- `CommentImage` (@Entity: "comment_images")

## 관련 테스트 클래스 (10개)

- `CommentTest`, `CommentMemberContextTest`
- `CommentRepositoryTest`, `CommentImageRepositoryTest`
- `CommentApplicationServiceTest`, `CommentServiceTest`, `CommentImageServiceTest`
- `MyGroupCommentPageFacadeServiceTest`
- `CommentCompositionTest`
- `CommentControllerTest` (E2E)

## DB 마이그레이션

- V4: `V4__alter_comments__mysql.sql`
- V18, V19: `V18/V19__create_comments__mysql.sql`
- V20: `V20__create_comments_index__mysql.sql`
- V31: `V31__alter_comments_for_groups.sql`
