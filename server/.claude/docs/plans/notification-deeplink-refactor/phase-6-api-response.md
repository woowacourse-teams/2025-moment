# Phase 6: 알림 조회 API + SSE 응답 DTO 변경

> 의존성: Phase 3 완료 (Notification 엔티티 변경됨)
> 예상 파일: 3개 수정 (2개 DTO + 1개 삭제)

## 목표

알림 조회 API 응답과 SSE 응답에서 `targetType`, `targetId`, `groupId`를 제거하고 `link`를 추가한다.
`NotificationPayload` 클래스를 삭제한다.

---

## Task 6.1: NotificationResponse 변경

### 파일
- 수정: `src/main/java/moment/notification/dto/response/NotificationResponse.java`

### AS-IS

```java
@Schema(description = "알림 응답")
public record NotificationResponse(
    @Schema(description = "알림 id", example = "1")
    Long id,

    @Schema(description = "알림 타입", example = "NEW_COMMENT_ON_MOMENT")
    NotificationType notificationType,

    @Schema(description = "타겟 타입", example = "MOMENT")
    TargetType targetType,

    @Schema(description = "타겟 id", example = "1")
    Long targetId,

    @Schema(description = "그룹 id", example = "1")
    Long groupId,

    @Schema(description = "메시지", example = "알림이 전송되었습니다.")
    String message,

    @Schema(description = "읽음 여부", example = "false")
    boolean isRead
) {
    public static NotificationResponse from(Notification notification) {
        NotificationType notificationType = notification.getNotificationType();
        return new NotificationResponse(
            notification.getId(), notificationType,
            notification.getTargetType(), notification.getTargetId(),
            notification.getGroupId(), notificationType.getMessage(),
            notification.isRead());
    }
}
```

### TO-BE

```java
@Schema(description = "알림 응답")
public record NotificationResponse(
    @Schema(description = "알림 id", example = "1")
    Long id,

    @Schema(description = "알림 타입", example = "NEW_COMMENT_ON_MOMENT")
    NotificationType notificationType,

    @Schema(description = "메시지", example = "내 모멘트에 새로운 코멘트가 달렸습니다.")
    String message,

    @Schema(description = "읽음 여부", example = "false")
    boolean isRead,

    @Schema(description = "딥링크", example = "/moments/1")
    String link
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getNotificationType(),
            notification.getNotificationType().getMessage(),
            notification.isRead(),
            notification.getLink());
    }
}
```

### 변경 사항
- 삭제: `targetType` (TargetType), `targetId` (Long), `groupId` (Long)
- 추가: `link` (String)
- Import 삭제: `import moment.global.domain.TargetType;`

---

## Task 6.2: NotificationSseResponse 변경

### 파일
- 수정: `src/main/java/moment/notification/dto/response/NotificationSseResponse.java`

### AS-IS

```java
@Schema(description = "SSE 알림 응답")
public record NotificationSseResponse(
    @Schema(description = "알림 id", example = "1")
    Long notificationId,

    @Schema(description = "알림 타입", example = "NEW_COMMENT_ON_MOMENT")
    NotificationType notificationType,

    @Schema(description = "타겟 타입", example = "MOMENT")
    TargetType targetType,

    @Schema(description = "타겟 id", example = "1")
    Long targetId,

    @Schema(description = "그룹 id", example = "1")
    Long groupId,

    @Schema(description = "메시지", example = "알림이 전송되었습니다.")
    String message,

    @Schema(description = "읽음 여부", example = "false")
    boolean isRead,

    @Schema(description = "딥링크", example = "/moments/1")
    String link
) {
    public static NotificationSseResponse of(NotificationPayload payload) {
        return new NotificationSseResponse(
            payload.notificationId(), payload.notificationType(),
            payload.targetType(), payload.targetId(), payload.groupId(),
            payload.message(), false, payload.link());
    }
}
```

### TO-BE

```java
@Schema(description = "SSE 알림 응답")
public record NotificationSseResponse(
    @Schema(description = "알림 id", example = "1")
    Long notificationId,

    @Schema(description = "알림 타입", example = "NEW_COMMENT_ON_MOMENT")
    NotificationType notificationType,

    @Schema(description = "메시지", example = "내 모멘트에 새로운 코멘트가 달렸습니다.")
    String message,

    @Schema(description = "딥링크", example = "/moments/1")
    String link
) {
    public static NotificationSseResponse from(Notification notification) {
        return new NotificationSseResponse(
            notification.getId(),
            notification.getNotificationType(),
            notification.getNotificationType().getMessage(),
            notification.getLink());
    }
}
```

### 변경 사항
- 삭제: `targetType`, `targetId`, `groupId`, `isRead`
- `of(NotificationPayload)` → `from(Notification)` 팩토리 변경
- Import 삭제: `import moment.global.domain.TargetType;`
- Import 삭제: `import moment.notification.domain.NotificationPayload;`
- Import 추가: `import moment.notification.domain.Notification;`

---

## Task 6.3: NotificationPayload 삭제

### 파일
- 삭제: `src/main/java/moment/notification/domain/NotificationPayload.java`

### 이유
- `NotificationSseResponse.from(Notification)` 이 직접 엔티티에서 변환하므로 중간 VO 불필요
- `buildLink()` 로직은 `DeepLinkGenerator`로 이동됨
- Facade에서 `NotificationPayload.from()` 호출 제거됨 (Phase 3)

---

## 완료 조건

- [ ] `NotificationResponse`에서 `targetType`, `targetId`, `groupId` 제거, `link` 추가
- [ ] `NotificationSseResponse`에서 `targetType`, `targetId`, `groupId`, `isRead` 제거, `from(Notification)` 추가
- [ ] `NotificationPayload.java` 삭제
- [ ] 모든 `TargetType` import이 notification 도메인에서 제거됨
