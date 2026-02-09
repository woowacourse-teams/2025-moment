# Phase 6: 알림 필터링

- **Status**: DRAFT
- **Created**: 2026-02-09
- **Parent Plan**: [user-block-plan.md](../user-block-plan.md)
- **Depends On**: Phase 1, Phase 2

---

## 목표

차단된 사용자로부터의 알림을 차단한다. 또한 Like 이벤트 레코드를 구조적으로 개선한다.

---

## 설계 근거

Phase 5에서 생성 자체를 차단하므로 알림 이벤트가 발행되지 않는 경우가 대부분이다. 하지만 race condition (차단 직후 이벤트가 이미 발행된 경우)이나 향후 확장을 위해 핸들러 차단도 유지한다.

---

## 현재 상태 분석

### NotificationEventHandler

**파일**: `src/main/java/moment/notification/service/eventHandler/NotificationEventHandler.java`

**차단 적용 대상 (4개)**:

| # | 핸들러 | 발신자 userId | 수신자 userId | 비고 |
|---|--------|-------------|-------------|------|
| 1 | `handleCommentCreateEvent` | `event.commenterId()` | `event.momenterId()` | 바로 사용 가능 |
| 2 | `handleGroupCommentCreateEvent` | `event.commenterId()` | `event.momentOwnerId()` | 바로 사용 가능 |
| 3 | `handleMomentLikeEvent` | 없음 (추가 필요) | `event.momentOwnerId()` | `likerUserId` 필드 추가 필요 |
| 4 | `handleCommentLikeEvent` | 없음 (추가 필요) | `event.commentOwnerId()` | `likerUserId` 필드 추가 필요 |

**차단 미적용 (3개)**: `handleGroupJoinRequestEvent`, `handleGroupJoinApprovedEvent`, `handleGroupKickedEvent`

### MomentLikeEvent (현재 상태)

**파일**: `src/main/java/moment/like/dto/event/MomentLikeEvent.java`

```java
public record MomentLikeEvent(
    Long momentId,
    Long momentOwnerId,
    Long likeMemberId,
    String likerNickname,
    Long groupId
) {}
```

- `likerUserId` 필드 없음
- `of()` 팩토리 메서드 없음 (CommentCreateEvent에는 있음)

### CommentLikeEvent (현재 상태)

**파일**: `src/main/java/moment/like/dto/event/CommentLikeEvent.java`

```java
public record CommentLikeEvent(
    Long commentId,
    Long commentOwnerId,
    Long likeMemberId,
    String likerNickname,
    Long groupId
) {}
```

- 동일하게 `likerUserId` 필드와 `of()` 팩토리 없음

### MomentLikeService.toggle() 이벤트 발행부

```java
eventPublisher.publishEvent(new MomentLikeEvent(
    moment.getId(),
    moment.getMomenter().getId(),
    member.getId(),
    member.getNickname(),
    moment.getGroup().getId()
));
```

### CommentLikeService.toggle() 이벤트 발행부

```java
eventPublisher.publishEvent(new CommentLikeEvent(
    comment.getId(),
    comment.getCommenter().getId(),
    member.getId(),
    member.getNickname(),
    member.getGroup().getId()
));
```

---

## TDD 테스트 목록

### 6-1. 이벤트 레코드 테스트 (구조적 변경)

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `MomentLikeEvent_of_팩토리로_생성한다` | of() 메서드 확인 |
| T2 | `MomentLikeEvent에_likerUserId가_포함된다` | 필드 확인 |
| T3 | `CommentLikeEvent_of_팩토리로_생성한다` | of() 메서드 확인 |
| T4 | `CommentLikeEvent에_likerUserId가_포함된다` | 필드 확인 |

### 6-2. NotificationEventHandler 테스트 (행동적 변경)

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `댓글_이벤트_차단된_사용자면_알림을_보내지_않는다` | skip 확인 |
| T2 | `그룹_댓글_이벤트_차단된_사용자면_알림을_보내지_않는다` | skip 확인 |
| T3 | `모멘트_좋아요_이벤트_차단된_사용자면_알림을_보내지_않는다` | skip 확인 |
| T4 | `댓글_좋아요_이벤트_차단된_사용자면_알림을_보내지_않는다` | skip 확인 |
| T5 | `self_notification은_차단_확인_전에_스킵된다` | DB 호출 절감 확인 |

---

## 구현 단계

### Step 1: Like 이벤트 레코드 구조 변경 (별도 커밋 - Tidy First)

**파일 수정**: `src/main/java/moment/like/dto/event/MomentLikeEvent.java`

```java
package moment.like.dto.event;

import moment.group.domain.GroupMember;
import moment.moment.domain.Moment;

public record MomentLikeEvent(
    Long momentId,
    Long momentOwnerId,
    Long likeMemberId,
    String likerNickname,
    Long groupId,
    Long likerUserId
) {
    public static MomentLikeEvent of(Moment moment, GroupMember member) {
        return new MomentLikeEvent(
            moment.getId(),
            moment.getMomenter().getId(),
            member.getId(),
            member.getNickname(),
            moment.getGroup().getId(),
            member.getUser().getId()
        );
    }
}
```

**파일 수정**: `src/main/java/moment/like/dto/event/CommentLikeEvent.java`

```java
package moment.like.dto.event;

import moment.comment.domain.Comment;
import moment.group.domain.GroupMember;

public record CommentLikeEvent(
    Long commentId,
    Long commentOwnerId,
    Long likeMemberId,
    String likerNickname,
    Long groupId,
    Long likerUserId
) {
    public static CommentLikeEvent of(Comment comment, GroupMember member) {
        return new CommentLikeEvent(
            comment.getId(),
            comment.getCommenter().getId(),
            member.getId(),
            member.getNickname(),
            member.getGroup().getId(),
            member.getUser().getId()
        );
    }
}
```

### Step 2: Like 서비스에서 of() 팩토리 사용 (별도 커밋 - Tidy First)

**파일 수정**: `src/main/java/moment/like/service/MomentLikeService.java`

AS-IS:
```java
eventPublisher.publishEvent(new MomentLikeEvent(
    moment.getId(), moment.getMomenter().getId(),
    member.getId(), member.getNickname(), moment.getGroup().getId()
));
```

TO-BE:
```java
eventPublisher.publishEvent(MomentLikeEvent.of(moment, member));
```

**파일 수정**: `src/main/java/moment/like/service/CommentLikeService.java`

AS-IS:
```java
eventPublisher.publishEvent(new CommentLikeEvent(
    comment.getId(), comment.getCommenter().getId(),
    member.getId(), member.getNickname(), member.getGroup().getId()
));
```

TO-BE:
```java
eventPublisher.publishEvent(CommentLikeEvent.of(comment, member));
```

> **Tidy First**: Step 1 + Step 2는 구조적 변경이므로 행동 변경(Step 3)과 별도 커밋으로 분리

### Step 3: NotificationEventHandler 차단 확인 추가 (행동적 변경)

**파일 수정**: `src/main/java/moment/notification/service/eventHandler/NotificationEventHandler.java`

의존성 추가:
```java
private final UserBlockApplicationService userBlockApplicationService;
```

각 핸들러에 self-notification skip + 차단 관계 확인 추가:

```java
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleCommentCreateEvent(CommentCreateEvent event) {
    // 1. self-notification skip (DB 호출 없음)
    if (event.commenterId().equals(event.momenterId())) {
        return;
    }
    // 2. 차단 관계 확인 (DB 호출)
    if (userBlockApplicationService.isBlocked(event.commenterId(), event.momenterId())) {
        log.info("Skipping notification due to block: commenter={}, momentOwner={}",
            event.commenterId(), event.momenterId());
        return;
    }
    // 3. 기존 알림 로직
}
```

동일 패턴으로 `handleGroupCommentCreateEvent`, `handleMomentLikeEvent`, `handleCommentLikeEvent` 수정.

---

## 생성/수정 파일 목록

| 작업 | 파일 경로 | 변경 내용 | 커밋 분류 |
|------|----------|----------|----------|
| 수정 | `src/main/java/moment/like/dto/event/MomentLikeEvent.java` | `likerUserId` + `of()` | 구조적 |
| 수정 | `src/main/java/moment/like/dto/event/CommentLikeEvent.java` | `likerUserId` + `of()` | 구조적 |
| 수정 | `src/main/java/moment/like/service/MomentLikeService.java` | `of()` 팩토리 사용 | 구조적 |
| 수정 | `src/main/java/moment/like/service/CommentLikeService.java` | `of()` 팩토리 사용 | 구조적 |
| 수정 | `src/main/java/moment/notification/service/eventHandler/NotificationEventHandler.java` | 차단 확인 추가 | 행동적 |

## 의존성

- Phase 2 완료 필수 (`UserBlockApplicationService.isBlocked()`)
- Like 이벤트 구조 변경 시 기존 `MomentLikeServiceTest` 수정 필요 (이벤트 캡처 검증부)

## 주의사항

- **Tidy First 분리**: Step 1-2 (구조적 변경) 커밋 -> Step 3 (행동적 변경) 커밋
- self-notification skip을 차단 확인 **이전에** 배치하여 불필요한 DB 호출 방지
- 기존 `MomentLikeServiceTest`의 이벤트 캡처 테스트 수정 필요: `MomentLikeEvent`에 `likerUserId` 필드 추가로 assertion 변경
