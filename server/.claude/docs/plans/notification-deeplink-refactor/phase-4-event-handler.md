# Phase 4: NotificationEventHandler 전체 핸들러 변경

> 의존성: Phase 3 완료 (NotificationCommand 변경됨)
> 예상 파일: 1개 프로덕션 수정

## 목표

모든 이벤트 핸들러에서 TargetType 참조를 제거하고, SourceData 기반으로 NotificationCommand를 생성한다.

---

## 파일
- 수정: `src/main/java/moment/notification/service/eventHandler/NotificationEventHandler.java`

## 핸들러별 변경 상세

### 1. handleCommentCreateEvent (개인 모멘트 댓글)

**AS-IS**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.momenterId(), event.momentId(),
    NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
    null, PushNotificationMessage.REPLY_TO_MOMENT));
```

**TO-BE**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.momenterId(),
    NotificationType.NEW_COMMENT_ON_MOMENT,
    SourceData.of(Map.of("momentId", event.momentId())),
    PushNotificationMessage.REPLY_TO_MOMENT));
```

### 2. handleGroupCommentCreateEvent (그룹 모멘트 댓글)

**AS-IS**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.momentOwnerId(), event.momentId(),
    NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
    event.groupId(), PushNotificationMessage.REPLY_TO_MOMENT));
```

**TO-BE**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.momentOwnerId(),
    NotificationType.NEW_COMMENT_ON_MOMENT,
    SourceData.of(Map.of(
        "momentId", event.momentId(),
        "groupId", event.groupId())),
    PushNotificationMessage.REPLY_TO_MOMENT));
```

### 3. handleGroupJoinRequestEvent

**AS-IS**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.ownerId(), event.groupId(),
    NotificationType.GROUP_JOIN_REQUEST, TargetType.GROUP,
    event.groupId(), PushNotificationMessage.GROUP_JOIN_REQUEST));
```

**TO-BE**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.ownerId(),
    NotificationType.GROUP_JOIN_REQUEST,
    SourceData.of(Map.of("groupId", event.groupId())),
    PushNotificationMessage.GROUP_JOIN_REQUEST));
```

### 4. handleGroupJoinApprovedEvent

**AS-IS**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.userId(), event.groupId(),
    NotificationType.GROUP_JOIN_APPROVED, TargetType.GROUP,
    event.groupId(), PushNotificationMessage.GROUP_JOIN_APPROVED));
```

**TO-BE**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.userId(),
    NotificationType.GROUP_JOIN_APPROVED,
    SourceData.of(Map.of("groupId", event.groupId())),
    PushNotificationMessage.GROUP_JOIN_APPROVED));
```

### 5. handleGroupKickedEvent

**AS-IS**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.kickedUserId(), event.groupId(),
    NotificationType.GROUP_KICKED, TargetType.GROUP,
    event.groupId(), PushNotificationMessage.GROUP_KICKED));
```

**TO-BE**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.kickedUserId(),
    NotificationType.GROUP_KICKED,
    SourceData.of(Map.of("groupId", event.groupId())),
    PushNotificationMessage.GROUP_KICKED));
```

### 6. handleMomentLikeEvent

**AS-IS**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.momentOwnerId(), event.momentId(),
    NotificationType.MOMENT_LIKED, TargetType.MOMENT,
    null, PushNotificationMessage.MOMENT_LIKED));
```

**TO-BE**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.momentOwnerId(),
    NotificationType.MOMENT_LIKED,
    SourceData.of(Map.of("momentId", event.momentId())),
    PushNotificationMessage.MOMENT_LIKED));
```

### 7. handleCommentLikeEvent

**AS-IS**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.commentOwnerId(), event.commentId(),
    NotificationType.COMMENT_LIKED, TargetType.COMMENT,
    null, PushNotificationMessage.COMMENT_LIKED));
```

**TO-BE**:
```java
notificationFacadeService.notify(new NotificationCommand(
    event.commentOwnerId(),
    NotificationType.COMMENT_LIKED,
    SourceData.of(Map.of("commentId", event.commentId())),
    PushNotificationMessage.COMMENT_LIKED));
```

---

## Import 변경

```diff
- import moment.global.domain.TargetType;
+ import moment.notification.domain.SourceData;
+ import java.util.Map;
```

---

## 완료 조건

- [ ] 7개 이벤트 핸들러 모두 TargetType 제거, SourceData 사용
- [ ] `TargetType` import 삭제
- [ ] `SourceData`, `Map` import 추가
