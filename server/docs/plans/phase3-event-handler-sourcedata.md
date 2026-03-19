# Phase 3: NotificationEventHandler SourceData 수정

> Parent: [plan.md](./plan.md)
> Status: 구현 대기
> 선행 조건: Phase 2 완료
> 대상 파일: `NotificationEventHandler.java`, `NotificationEventHandlerTest.java`

## 목표

NotificationEventHandler에서 MomentLikeEvent, CommentLikeEvent 처리 시
SourceData에 groupId를 포함하도록 수정한다.

---

## Step 3-1: handleMomentLikeEvent 테스트 수정 (RED)

**파일**: `src/test/java/moment/notification/service/eventHandler/NotificationEventHandlerTest.java:100-114`

```java
// 현재
@Test
void 모멘트_좋아요_이벤트_시_알림을_발송한다() {
    // given
    MomentLikeEvent event = new MomentLikeEvent(1L, 2L, 3L, "좋아요닉네임");

    // when
    eventHandler.handleMomentLikeEvent(event);

    // then
    verify(notificationFacadeService).notify(new NotificationCommand(
            2L,
            NotificationType.MOMENT_LIKED,
            SourceData.of(Map.of("momentId", 1L)),
            PushNotificationMessage.MOMENT_LIKED));
}

// 수정 후
@Test
void 모멘트_좋아요_이벤트_시_알림을_발송한다() {
    // given
    MomentLikeEvent event = new MomentLikeEvent(1L, 2L, 3L, "좋아요닉네임", 10L);

    // when
    eventHandler.handleMomentLikeEvent(event);

    // then
    verify(notificationFacadeService).notify(new NotificationCommand(
            2L,
            NotificationType.MOMENT_LIKED,
            SourceData.of(Map.of("momentId", 1L, "groupId", 10L)),
            PushNotificationMessage.MOMENT_LIKED));
}
```

**변경 사항**:
1. `MomentLikeEvent` 생성자에 5번째 인자 `10L` (groupId) 추가
2. 기대 `SourceData`에 `"groupId", 10L` 추가

**실행**: `./gradlew test --tests "moment.notification.service.eventHandler.NotificationEventHandlerTest.모멘트_좋아요_이벤트_시_알림을_발송한다"` → FAIL (SourceData 불일치)

---

## Step 3-2: handleMomentLikeEvent 구현 수정 (GREEN)

**파일**: `src/main/java/moment/notification/service/eventHandler/NotificationEventHandler.java:84-93`

```java
// 현재
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleMomentLikeEvent(MomentLikeEvent event) {
    log.info("MomentLikeEvent received: momentId={}, liker={}",
        event.momentId(), event.likerNickname());

    notificationFacadeService.notify(new NotificationCommand(
            event.momentOwnerId(),
            NotificationType.MOMENT_LIKED,
            SourceData.of(Map.of("momentId", event.momentId())),
            PushNotificationMessage.MOMENT_LIKED));
}

// 수정 후
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleMomentLikeEvent(MomentLikeEvent event) {
    log.info("MomentLikeEvent received: momentId={}, liker={}",
        event.momentId(), event.likerNickname());

    notificationFacadeService.notify(new NotificationCommand(
            event.momentOwnerId(),
            NotificationType.MOMENT_LIKED,
            SourceData.of(Map.of("momentId", event.momentId(), "groupId", event.groupId())),
            PushNotificationMessage.MOMENT_LIKED));
}
```

**변경**: `SourceData.of(Map.of(...))` 에 `"groupId", event.groupId()` 추가

**실행**: 같은 테스트 → PASS 확인

---

## Step 3-3: handleCommentLikeEvent 테스트 수정 (RED)

**파일**: `NotificationEventHandlerTest.java:116-130`

```java
// 현재
@Test
void 코멘트_좋아요_이벤트_시_알림을_발송한다() {
    // given
    CommentLikeEvent event = new CommentLikeEvent(1L, 2L, 3L, "좋아요닉네임");

    // when
    eventHandler.handleCommentLikeEvent(event);

    // then
    verify(notificationFacadeService).notify(new NotificationCommand(
            2L,
            NotificationType.COMMENT_LIKED,
            SourceData.of(Map.of("commentId", 1L)),
            PushNotificationMessage.COMMENT_LIKED));
}

// 수정 후
@Test
void 코멘트_좋아요_이벤트_시_알림을_발송한다() {
    // given
    CommentLikeEvent event = new CommentLikeEvent(1L, 2L, 3L, "좋아요닉네임", 10L);

    // when
    eventHandler.handleCommentLikeEvent(event);

    // then
    verify(notificationFacadeService).notify(new NotificationCommand(
            2L,
            NotificationType.COMMENT_LIKED,
            SourceData.of(Map.of("commentId", 1L, "groupId", 10L)),
            PushNotificationMessage.COMMENT_LIKED));
}
```

**변경 사항**:
1. `CommentLikeEvent` 생성자에 5번째 인자 `10L` (groupId) 추가
2. 기대 `SourceData`에 `"groupId", 10L` 추가

**실행**: `./gradlew test --tests "moment.notification.service.eventHandler.NotificationEventHandlerTest.코멘트_좋아요_이벤트_시_알림을_발송한다"` → FAIL

---

## Step 3-4: handleCommentLikeEvent 구현 수정 (GREEN)

**파일**: `NotificationEventHandler.java:96-106`

```java
// 현재
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleCommentLikeEvent(CommentLikeEvent event) {
    log.info("CommentLikeEvent received: commentId={}, liker={}",
        event.commentId(), event.likerNickname());

    notificationFacadeService.notify(new NotificationCommand(
            event.commentOwnerId(),
            NotificationType.COMMENT_LIKED,
            SourceData.of(Map.of("commentId", event.commentId())),
            PushNotificationMessage.COMMENT_LIKED));
}

// 수정 후
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleCommentLikeEvent(CommentLikeEvent event) {
    log.info("CommentLikeEvent received: commentId={}, liker={}",
        event.commentId(), event.likerNickname());

    notificationFacadeService.notify(new NotificationCommand(
            event.commentOwnerId(),
            NotificationType.COMMENT_LIKED,
            SourceData.of(Map.of("commentId", event.commentId(), "groupId", event.groupId())),
            PushNotificationMessage.COMMENT_LIKED));
}
```

**변경**: `SourceData.of(Map.of(...))` 에 `"groupId", event.groupId()` 추가

**실행**: 같은 테스트 → PASS 확인

---

## Step 3-5: 전체 NotificationEventHandler 테스트 검증

**실행**: `./gradlew test --tests "moment.notification.service.eventHandler.NotificationEventHandlerTest"` → ALL PASS (7개 테스트)

기존 테스트 중 영향받지 않는 것들:
- `코멘트_생성_이벤트_시_알림을_발송한다` - CommentCreateEvent 무관
- `그룹_가입_신청_이벤트_시_알림을_발송한다` - GroupJoinRequestEvent 무관
- `그룹_가입_승인_이벤트_시_알림을_발송한다` - GroupJoinApprovedEvent 무관
- `그룹_강퇴_이벤트_시_알림을_발송한다` - GroupKickedEvent 무관
- `그룹_코멘트_생성_이벤트_시_알림을_발송한다` - GroupCommentCreateEvent 무관

---

## 최종 결과물

### 수정된 프로덕션 파일 (1개)

| 파일 | 메서드 | 변경 내용 |
|------|--------|----------|
| `NotificationEventHandler.java` | `handleMomentLikeEvent` | SourceData에 `groupId` 추가 |
| `NotificationEventHandler.java` | `handleCommentLikeEvent` | SourceData에 `groupId` 추가 |

### 수정된 테스트 파일 (1개)

| 파일 | 테스트 | 변경 내용 |
|------|--------|----------|
| `NotificationEventHandlerTest.java` | `모멘트_좋아요_이벤트_시_알림을_발송한다` | 이벤트 생성자 + SourceData 기대값 수정 |
| `NotificationEventHandlerTest.java` | `코멘트_좋아요_이벤트_시_알림을_발송한다` | 이벤트 생성자 + SourceData 기대값 수정 |
