# Block Domain (PREFIX: BLK)

> Last Updated: 2026-02-09
> Features: 6

## 기능 목록

### BLK-001: 사용자 차단

- **Status**: DONE
- **API**: `POST /api/v2/users/{userId}/blocks`
- **Key Classes**:
    - Controller: `UserBlockController`
    - Application: `UserBlockApplicationService`
    - Domain: `UserBlockService`
    - Entity: `UserBlock`
    - DTO: `UserBlockResponse`
- **Business Rules**: 자기 자신 차단 불가, 이미 차단된 사용자 재차단 불가, soft delete된 차단 관계 복원(restore) 지원
- **Dependencies**: user (`UserService`)
- **Tests**: `UserBlockTest`, `UserBlockServiceTest`, `UserBlockApplicationServiceTest`, `UserBlockControllerTest` (E2E)
- **Error Codes**: BL-001 (자기 자신 차단), BL-002 (이미 차단됨)

### BLK-002: 사용자 차단 해제

- **Status**: DONE
- **API**: `DELETE /api/v2/users/{userId}/blocks`
- **Key Classes**:
    - Controller: `UserBlockController`
    - Application: `UserBlockApplicationService`
    - Domain: `UserBlockService`
- **Business Rules**: 차단 관계가 존재하지 않으면 에러, soft delete로 처리
- **Dependencies**: user (`UserService`)
- **Tests**: `UserBlockServiceTest`, `UserBlockApplicationServiceTest`, `UserBlockControllerTest` (E2E)
- **Error Codes**: BL-003 (차단 관계 미존재)

### BLK-003: 차단 목록 조회

- **Status**: DONE
- **API**: `GET /api/v2/users/blocks`
- **Key Classes**:
    - Controller: `UserBlockController`
    - Application: `UserBlockApplicationService`
    - Domain: `UserBlockService`
    - DTO: `UserBlockListResponse`
- **Business Rules**: JOIN FETCH로 N+1 방지, 차단된 사용자 닉네임 포함
- **Dependencies**: user (`UserService`)
- **Tests**: `UserBlockServiceTest`, `UserBlockApplicationServiceTest`, `UserBlockControllerTest` (E2E)
- **Error Codes**: -

### BLK-004: 차단된 사용자 모멘트 피드 필터링

- **Status**: DONE
- **API**: (기존 그룹 모멘트 피드 API에 필터 적용)
- **Key Classes**:
    - Domain: `MomentService` (blockedUserIds 파라미터 추가)
    - Application: `MomentApplicationService`
    - Facade: `MyGroupMomentPageFacadeService`
    - Repository: `MomentRepository` (NOT IN 조건 추가)
- **Business Rules**: 양방향 차단 관계의 사용자 모멘트를 그룹 피드에서 제외, JPQL NOT IN 빈 리스트 방어용 센티널 값(-1L) 사용
- **Dependencies**: moment (`MomentService`, `MomentRepository`)
- **Tests**: `MomentServiceTest`, `MomentApplicationServiceTest`, `MyGroupMomentPageFacadeServiceTest`
- **Error Codes**: -

### BLK-005: 차단된 사용자 간 상호작용 차단

- **Status**: DONE
- **API**: (기존 댓글/좋아요 API에 차단 검증 추가)
- **Key Classes**:
    - Facade: `CommentCreateFacadeService`, `GroupCommentCreateFacadeService`
    - Application: `CommentApplicationService`, `MomentApplicationService`
    - Repository: `UserBlockRepository` (`existsBidirectionalBlock`)
- **Business Rules**: 차단된 사용자의 모멘트에 댓글 작성 불가, 차단된 사용자의 모멘트/댓글에 좋아요 불가, 양방향 차단 확인
- **Dependencies**: comment (`CommentCreateFacadeService`, `GroupCommentCreateFacadeService`), moment (`MomentApplicationService`)
- **Tests**: `CommentCreateFacadeServiceTest`, `GroupCommentCreateFacadeServiceTest`, `MomentApplicationServiceTest`, `CommentApplicationServiceTest`
- **Error Codes**: BL-004 (차단된 사용자 상호작용 불가)

### BLK-006: 차단된 사용자 알림/댓글 필터링

- **Status**: DONE
- **API**: (기존 알림/댓글 조회 API에 필터 적용)
- **Key Classes**:
    - EventHandler: `NotificationEventHandler` (차단 확인 후 알림 스킵)
    - Facade: `MyGroupCommentPageFacadeService` (댓글 목록에서 차단 사용자 제외)
    - Application: `CommentApplicationService` (그룹 댓글 조회 시 필터링)
    - DTO: `CommentComposition` (`commenterUserId` 필드 추가)
- **Business Rules**: 차단된 사용자의 댓글/좋아요 알림 미발송, 댓글 목록에서 차단 사용자 댓글 제외, 댓글 수 카운트에서 차단 사용자 제외
- **Dependencies**: notification (`NotificationEventHandler`), comment (`CommentApplicationService`, `CommentService`)
- **Tests**: `NotificationEventHandlerTest`, `MyGroupCommentPageFacadeServiceTest`, `CommentApplicationServiceTest`
- **Error Codes**: -

## Domain Events Published

| Event | 구독자 | 설명 | 상태 |
|-------|--------|------|------|
| (없음) | - | block 도메인은 이벤트를 발행하지 않음 | - |

## 관련 에러 코드

| 코드 | 메시지 | HTTP 상태 |
|------|--------|-----------|
| BL-001 | 자기 자신을 차단할 수 없습니다. | 400 Bad Request |
| BL-002 | 이미 차단된 사용자입니다. | 409 Conflict |
| BL-003 | 차단 관계가 존재하지 않습니다. | 404 Not Found |
| BL-004 | 차단된 사용자와 상호작용할 수 없습니다. | 403 Forbidden |

## 관련 엔티티

- `UserBlock` (@Entity: "user_blocks") - Soft Delete, blocker-blockedUser 복합 유니크 제약

## 크로스 도메인 영향

block 도메인의 `UserBlockApplicationService`를 다음 도메인에서 의존:
- **moment**: `MomentService`, `MomentApplicationService`, `MomentRepository`, `MyGroupMomentPageFacadeService` - 피드 필터링, 좋아요 차단
- **comment**: `CommentCreateFacadeService`, `GroupCommentCreateFacadeService`, `CommentApplicationService`, `CommentService`, `CommentRepository`, `MyGroupCommentPageFacadeService` - 댓글 생성 차단, 댓글 필터링
- **like**: `MomentLikeEvent`, `CommentLikeEvent` - `likerUserId` 필드 및 `of()` 팩토리 추가
- **notification**: `NotificationEventHandler` - 차단된 사용자 알림 스킵

## 관련 테스트 클래스 (4개)

- `UserBlockTest`, `UserBlockServiceTest`, `UserBlockApplicationServiceTest`
- `UserBlockControllerTest` (E2E)

## DB 마이그레이션

- V38: `V38__create_user_blocks.sql` - user_blocks 테이블 생성, blocker_id+blocked_user_id 유니크 제약, blocked_user_id 인덱스
