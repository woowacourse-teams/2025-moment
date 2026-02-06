# Phase 3: 서비스 레이어 + Repository 변경

> 의존성: Phase 2 완료
> 예상 파일: 5개 프로덕션 수정 + 1개 DTO 수정

## 목표

알림 생성/조회 흐름의 핵심 서비스 레이어를 새 구조로 변경한다.
TargetType 기반 쿼리를 NotificationType 기반으로 교체한다.

---

## Task 3.1: NotificationCommand 변경

### 파일
- 수정: `src/main/java/moment/notification/domain/NotificationCommand.java`

### AS-IS

```java
public record NotificationCommand(
    Long userId,
    Long targetId,
    NotificationType notificationType,
    TargetType targetType,
    Long groupId,
    PushNotificationMessage pushMessage
) {}
```

### TO-BE

```java
public record NotificationCommand(
    Long userId,
    NotificationType notificationType,
    SourceData sourceData,
    PushNotificationMessage pushMessage
) {}
```

### 변경 사항
- `targetId`, `targetType`, `groupId` 삭제
- `sourceData` 추가
- `TargetType` import 삭제

---

## Task 3.2: NotificationRepository 변경

### 파일
- 수정: `src/main/java/moment/notification/infrastructure/NotificationRepository.java`

### AS-IS 메서드 (삭제)

```java
@Query("""
    SELECT DISTINCT n.targetId
    FROM notifications n
    WHERE n.user.id = :userId AND n.isRead = :isRead AND n.targetType = :targetType
    """)
List<Long> findAllByUserIdAndIsReadAndTargetType(
    @Param("userId") Long userId,
    @Param("isRead") boolean isRead,
    @Param("targetType") TargetType targetType);

@Query("""
    SELECT n
    FROM notifications n
    WHERE n.targetId IN :targetIds AND n.isRead = :isRead AND n.targetType = :targetType
    """)
List<Notification> findNotificationsBy(
    @Param("targetIds") List<Long> targetIds,
    @Param("isRead") boolean isRead,
    @Param("targetType") TargetType targetType);
```

### TO-BE 메서드 (추가)

```java
// notification_type 기반 조회 (서비스 레이어에서 source_data 필터링)
List<Notification> findAllByUserIdAndIsReadAndNotificationTypeIn(
    Long userId, boolean isRead, List<NotificationType> notificationTypes);

List<Notification> findAllByIsReadAndNotificationTypeIn(
    boolean isRead, List<NotificationType> notificationTypes);
```

### Import 변경
- 삭제: `import moment.global.domain.TargetType;`
- `@Query` 어노테이션과 `@Param` import 삭제 (Spring Data 네이밍 컨벤션 사용)

---

## Task 3.3: NotificationService 변경

### 파일
- 수정: `src/main/java/moment/notification/service/notification/NotificationService.java`

### AS-IS 메서드

```java
public List<Long> getUnreadTargetIdsBy(Long userId, TargetType targetType) {
    return notificationRepository.findAllByUserIdAndIsReadAndTargetType(userId, false, targetType);
}

@Transactional
public Notification save(User user, Long targetId, NotificationType type,
                        TargetType targetType, Long groupId) {
    Notification notification = (groupId != null)
        ? new Notification(user, type, targetType, targetId, groupId)
        : new Notification(user, type, targetType, targetId);
    return notificationRepository.save(notification);
}

public List<Notification> getNotificationsBy(List<Long> targetIds, boolean isRead, TargetType targetType) {
    return notificationRepository.findNotificationsBy(targetIds, isRead, targetType);
}
```

### TO-BE 메서드

```java
@Transactional
public Notification save(User user, NotificationType notificationType,
                         SourceData sourceData, String link) {
    return notificationRepository.save(
        new Notification(user, notificationType, sourceData, link));
}

public List<Notification> getAllBy(Long userId, boolean isRead, List<NotificationType> types) {
    return notificationRepository.findAllByUserIdAndIsReadAndNotificationTypeIn(
        userId, isRead, types);
}

public List<Notification> getAllBy(boolean isRead, List<NotificationType> types) {
    return notificationRepository.findAllByIsReadAndNotificationTypeIn(isRead, types);
}
```

### 삭제 메서드
- `getUnreadTargetIdsBy(Long userId, TargetType targetType)`
- `getNotificationsBy(List<Long> targetIds, boolean isRead, TargetType targetType)`

### Import 변경
- 삭제: `import moment.global.domain.TargetType;`
- 추가: `import moment.notification.domain.SourceData;`
- 추가: `import java.util.List;` (기존에 있으면 유지)

---

## Task 3.4: NotificationApplicationService 변경

### 파일
- 수정: `src/main/java/moment/notification/service/application/NotificationApplicationService.java`

### AS-IS 메서드 (변경)

```java
@Transactional
public Notification createNotification(
        Long userId, Long targetId,
        NotificationType notificationType, TargetType targetType, Long groupId) {
    User user = userService.getUserBy(userId);
    return notificationService.save(user, targetId, notificationType, targetType, groupId);
}

public List<Long> getUnreadNotifications(Long userId, TargetType targetType) {
    return notificationService.getUnreadTargetIdsBy(userId, targetType);
}

public Map<Long, List<Long>> getNotificationsByTargetIdsAndTargetType(
        List<Long> targetIds, TargetType targetType) {
    ...
}
```

### TO-BE 메서드

```java
@Transactional
public Notification createNotification(Long userId, NotificationType notificationType,
                                        SourceData sourceData, String link) {
    User user = userService.getUserBy(userId);
    return notificationService.save(user, notificationType, sourceData, link);
}

// TargetType 기반 → NotificationType 기반 + 서비스 레이어 필터링
private static final List<NotificationType> MOMENT_TYPES =
    List.of(NotificationType.NEW_COMMENT_ON_MOMENT, NotificationType.MOMENT_LIKED);

private static final List<NotificationType> COMMENT_TYPES =
    List.of(NotificationType.COMMENT_LIKED);

public List<Long> getUnreadMomentIds(Long userId) {
    List<Notification> notifications = notificationService.getAllBy(
        userId, false, MOMENT_TYPES);
    return notifications.stream()
        .map(n -> n.getSourceData().getLong("momentId"))
        .filter(Objects::nonNull)
        .distinct()
        .toList();
}

public List<Long> getUnreadCommentIds(Long userId) {
    List<Notification> notifications = notificationService.getAllBy(
        userId, false, COMMENT_TYPES);
    return notifications.stream()
        .map(n -> n.getSourceData().getLong("commentId"))
        .filter(Objects::nonNull)
        .distinct()
        .toList();
}

public Map<Long, List<Long>> getNotificationsByMomentIds(List<Long> momentIds) {
    List<Notification> notifications = notificationService.getAllBy(false, MOMENT_TYPES);
    return groupNotificationIdsBySourceId(notifications, "momentId", momentIds);
}

public Map<Long, List<Long>> getNotificationsByCommentIds(List<Long> commentIds) {
    List<Notification> notifications = notificationService.getAllBy(false, COMMENT_TYPES);
    return groupNotificationIdsBySourceId(notifications, "commentId", commentIds);
}

private Map<Long, List<Long>> groupNotificationIdsBySourceId(
        List<Notification> notifications, String sourceKey, List<Long> targetIds) {
    Map<Long, List<Notification>> grouped = notifications.stream()
        .filter(n -> n.getSourceData().getLong(sourceKey) != null)
        .collect(Collectors.groupingBy(n -> n.getSourceData().getLong(sourceKey)));

    return targetIds.stream()
        .collect(Collectors.toMap(
            id -> id,
            id -> grouped.getOrDefault(id, List.of()).stream()
                .map(Notification::getId)
                .toList()));
}
```

### 삭제 메서드
- `getUnreadNotifications(Long userId, TargetType targetType)`
- `getNotificationsByTargetIdsAndTargetType(List<Long> targetIds, TargetType targetType)`
- `mapNotificationIdForMomentId(...)` private 메서드

### Import 변경
- 삭제: `import moment.global.domain.TargetType;`
- 추가: `import moment.notification.domain.SourceData;`
- 추가: `import java.util.Objects;`

---

## Task 3.5: NotificationFacadeService 변경

### 파일
- 수정: `src/main/java/moment/notification/service/facade/NotificationFacadeService.java`

### AS-IS

```java
public void notify(NotificationCommand command) {
    Notification savedNotification = notificationApplicationService.createNotification(
        command.userId(), command.targetId(), command.notificationType(),
        command.targetType(), command.groupId());

    NotificationPayload payload = NotificationPayload.from(savedNotification);
    sseNotificationService.sendToClient(command.userId(), "notification",
        NotificationSseResponse.of(payload));

    if (command.pushMessage() != null) {
        pushNotificationApplicationService.sendToDeviceEndpoint(
            command.userId(), command.pushMessage());
    }
}
```

### TO-BE

```java
public void notify(NotificationCommand command) {
    // 1. 딥링크 계산 (SourceData 기반)
    String link = DeepLinkGenerator.generate(
        command.notificationType(), command.sourceData());

    // 2. DB 저장 (sourceData + link 포함)
    Notification savedNotification = notificationApplicationService.createNotification(
        command.userId(), command.notificationType(),
        command.sourceData(), link);

    // 3. SSE 전송 (link 포함)
    NotificationSseResponse sseResponse = NotificationSseResponse.from(savedNotification);
    sseNotificationService.sendToClient(command.userId(), "notification", sseResponse);

    // 4. Push 전송 (link 포함)
    if (command.pushMessage() != null) {
        pushNotificationApplicationService.sendToDeviceEndpoint(
            command.userId(), command.pushMessage(), link);
    }
}
```

### Import 변경
- 삭제: `import moment.notification.domain.NotificationPayload;`
- 추가: `import moment.notification.domain.DeepLinkGenerator;`

> **참고**: `NotificationSseResponse.of(payload)` → `NotificationSseResponse.from(notification)` 으로 변경.
> 이 변경은 Phase 6에서 `NotificationSseResponse`를 수정할 때 적용.
> Phase 3에서는 `NotificationSseResponse.from()` 메서드가 아직 없으므로, Phase 6와 동시에 커밋하거나
> Phase 3에서 임시로 기존 방식 유지 가능.

---

## 완료 조건

- [ ] `NotificationCommand`에서 TargetType 관련 필드 삭제
- [ ] `NotificationRepository`에서 TargetType 쿼리 삭제, NotificationType 쿼리 추가
- [ ] `NotificationService.save()` 파라미터 변경
- [ ] `NotificationApplicationService`에 `getUnreadMomentIds()`, `getUnreadCommentIds()` 추가
- [ ] `NotificationFacadeService.notify()` SourceData 기반으로 변경
- [ ] ⚠️ Phase 4~7 완료 전까지 빌드 실패 상태 지속
