# Phase 5: Push 알림에 link 추가

> 의존성: Phase 3 완료 (NotificationFacadeService에서 link를 Push로 전달)
> 예상 파일: 3개 프로덕션 수정

## 목표

Push 알림(Expo Push)에 `link` 데이터를 포함시킨다.
기존에는 `title` + `body`만 전달했으나, `data.link`에 딥링크를 추가한다.

---

## Task 5.1: PushNotificationCommand 변경

### 파일
- 수정: `src/main/java/moment/notification/domain/PushNotificationCommand.java`

### AS-IS

```java
public record PushNotificationCommand(User user, PushNotificationMessage message) {}
```

### TO-BE

```java
public record PushNotificationCommand(
    User user,
    PushNotificationMessage message,
    String link
) {}
```

---

## Task 5.2: PushNotificationApplicationService 변경

### 파일
- 수정: `src/main/java/moment/notification/service/application/PushNotificationApplicationService.java`

### AS-IS

```java
public void sendToDeviceEndpoint(long userId, PushNotificationMessage message) {
    User user = userService.getUserBy(userId);
    pushNotificationSender.send(new PushNotificationCommand(user, message));
}
```

### TO-BE

```java
public void sendToDeviceEndpoint(long userId, PushNotificationMessage message, String link) {
    User user = userService.getUserBy(userId);
    pushNotificationSender.send(new PushNotificationCommand(user, message, link));
}
```

---

## Task 5.3: ExpoPushNotificationSender 변경

### 파일
- 수정: `src/main/java/moment/notification/infrastructure/expo/ExpoPushNotificationSender.java`

### AS-IS

```java
@Override
public void send(PushNotificationCommand command) {
    Long userId = command.user().getId();
    PushNotificationMessage message = command.message();

    List<String> deviceTokens = pushNotificationRepository.findByUserId(userId)
        .stream().map(PushNotification::getDeviceEndpoint).toList();

    if (deviceTokens.isEmpty()) { ... return; }

    List<ExpoPushMessage> messages = deviceTokens.stream()
        .map(token -> ExpoPushMessage.of(token, message, Map.of()))  // ← 빈 data
        .toList();
    ...
}
```

### TO-BE

```java
@Override
public void send(PushNotificationCommand command) {
    Long userId = command.user().getId();
    PushNotificationMessage message = command.message();
    String link = command.link();

    List<String> deviceTokens = pushNotificationRepository.findByUserId(userId)
        .stream().map(PushNotification::getDeviceEndpoint).toList();

    if (deviceTokens.isEmpty()) { ... return; }

    // link를 data에 포함
    Map<String, Object> data = (link != null)
        ? Map.of("link", link)
        : Map.of();

    List<ExpoPushMessage> messages = deviceTokens.stream()
        .map(token -> ExpoPushMessage.of(token, message, data))
        .toList();
    ...
}
```

### 변경 핵심
- `Map.of()` → `Map.of("link", link)` (link가 null이 아닐 때)
- `command.link()` 접근 추가

---

## 완료 조건

- [ ] `PushNotificationCommand`에 `link` 필드 추가
- [ ] `PushNotificationApplicationService.sendToDeviceEndpoint()`에 `link` 파라미터 추가
- [ ] `ExpoPushNotificationSender.send()`에서 `data`에 link 포함
