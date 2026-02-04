# Like Domain (PREFIX: LIK)

> Last Updated: 2026-02-04
> Features: 2

## 탈퇴 사용자 콘텐츠 처리

- `MomentLikeService`: momenter null 체크 추가 (탈퇴 사용자 모멘트에 좋아요 시 이벤트 발행 안 함)
- `CommentLikeService`: commenter null 체크 추가 (탈퇴 사용자 댓글에 좋아요 시 이벤트 발행 안 함)

## 기능 목록

### LIK-001: 모멘트 좋아요 토글

- **Status**: DONE
- **API**: `POST /api/v2/groups/{groupId}/moments/{momentId}/like`
- **Key Classes**:
    - Controller: `GroupMomentController`
    - Domain: `MomentLikeService`
    - Entity: `MomentLike`
- **Business Rules**: 좋아요 토글 (존재하면 삭제, 없으면 생성), UniqueConstraint(moment_id, member_id)
- **Dependencies**: group (GroupMember 검증)
- **Tests**: `MomentLikeTest`, `MomentLikeServiceTest`

### LIK-002: 댓글 좋아요 토글

- **Status**: DONE
- **API**: `POST /api/v2/groups/{groupId}/comments/{commentId}/like`
- **Key Classes**:
    - Controller: `GroupCommentController`
    - Domain: `CommentLikeService`
    - Entity: `CommentLike`
- **Business Rules**: 좋아요 토글 (존재하면 삭제, 없으면 생성), UniqueConstraint(comment_id, member_id)
- **Dependencies**: group (GroupMember 검증)
- **Tests**: `CommentLikeTest`, `CommentLikeServiceTest`

## Domain Events Published

| Event | 구독자 | 설명 |
|-------|--------|------|
| `MomentLikeEvent` | `NotificationEventHandler` | 모멘트 좋아요 시 작성자에게 알림 |
| `CommentLikeEvent` | `NotificationEventHandler` | 댓글 좋아요 시 작성자에게 알림 |

## 관련 엔티티

- `MomentLike` (@Entity: "moment_likes") - UniqueConstraint(moment_id, member_id)
- `CommentLike` (@Entity: "comment_likes") - UniqueConstraint(comment_id, member_id)

## 관련 테스트 클래스 (4개)

- `MomentLikeTest`, `CommentLikeTest`
- `MomentLikeServiceTest`, `CommentLikeServiceTest`

## DB 마이그레이션

- V32: `V32__create_likes.sql`
