# Phase 8: 테스트 코드 전면 갱신

> 의존성: Phase 1~7 전체 완료
> 예상 파일: 8개 테스트 수정 + 1개 삭제 + 1개 신규

## 목표

프로덕션 코드 변경에 맞춰 모든 테스트를 갱신한다.
`./gradlew fastTest` 전체 통과를 확인한다.

---

## Task 8.1: NotificationPayloadTest → 삭제

### 파일
- 삭제: `src/test/java/moment/notification/domain/NotificationPayloadTest.java`

### 이유
- `NotificationPayload` 클래스가 Phase 6에서 삭제됨
- 딥링크 생성 로직은 `DeepLinkGeneratorTest` (Phase 1에서 생성됨)로 대체

---

## Task 8.2: NotificationResponseTest 수정

### 파일
- 수정: `src/test/java/moment/notification/dto/response/NotificationResponseTest.java`

### AS-IS 테스트 (삭제)

```java
void groupId_포함_NotificationResponse_생성()
void groupId_null_NotificationResponse_생성()
```

### TO-BE 테스트

```java
@Test
void link가_포함된_Notification에서_NotificationResponse를_생성한다() {
    User user = UserFixture.createUser();
    SourceData sourceData = SourceData.of(Map.of("groupId", 3L));
    Notification notification = new Notification(
        user, NotificationType.GROUP_JOIN_REQUEST, sourceData, "/groups/3");

    NotificationResponse response = NotificationResponse.from(notification);

    assertThat(response.link()).isEqualTo("/groups/3");
    assertThat(response.notificationType()).isEqualTo(NotificationType.GROUP_JOIN_REQUEST);
    assertThat(response.message()).isEqualTo(NotificationType.GROUP_JOIN_REQUEST.getMessage());
}

@Test
void link가_null인_Notification에서_NotificationResponse를_생성한다() {
    User user = UserFixture.createUser();
    SourceData sourceData = SourceData.of(Map.of("groupId", 3L));
    Notification notification = new Notification(
        user, NotificationType.GROUP_KICKED, sourceData, null);

    NotificationResponse response = NotificationResponse.from(notification);

    assertThat(response.link()).isNull();
}
```

---

## Task 8.3: NotificationRepositoryTest 수정

### 파일
- 수정: `src/test/java/moment/notification/infrastructure/NotificationRepositoryTest.java`

### 변경 범위

**모든 `new Notification(user, reason, contentType, contentId)` 호출을 변경**:

AS-IS:
```java
new Notification(user, reason, contentType, contentId)
new Notification(user, reason, contentType, contentId, groupId)
```

TO-BE:
```java
new Notification(user, reason, SourceData.of(Map.of("momentId", contentId)), "/moments/" + contentId)
```

**삭제할 테스트**:
- `user_id와_타겟_타입을_고려하여_읽지_않은_알림의_컨텐츠_id들을_조회한다()` — TargetType 쿼리 삭제됨
- `user_id와_타겟_타입을_고려하여_읽은_알림의_컨텐츠_id들을_조회한다()` — TargetType 쿼리 삭제됨
- `타켓_id와_타겟_타입으로_읽지_않은_알림_목록을_조회한다()` — TargetType 쿼리 삭제됨
- `타켓_id와_타겟_타입으로_읽은_알림_목록을_조회한다()` — TargetType 쿼리 삭제됨

**추가할 테스트**:

```java
@Test
void notification_type_목록으로_읽지_않은_알림을_조회한다() {
    // given
    Notification momentNotification = new Notification(user,
        NotificationType.NEW_COMMENT_ON_MOMENT,
        SourceData.of(Map.of("momentId", 42L)), "/moments/42");
    Notification likeNotification = new Notification(user,
        NotificationType.MOMENT_LIKED,
        SourceData.of(Map.of("momentId", 43L)), "/moments/43");
    Notification commentNotification = new Notification(user,
        NotificationType.COMMENT_LIKED,
        SourceData.of(Map.of("commentId", 10L)), "/comments/10");
    Notification readNotification = new Notification(user,
        NotificationType.NEW_COMMENT_ON_MOMENT,
        SourceData.of(Map.of("momentId", 44L)), "/moments/44");
    readNotification.markAsRead();

    notificationRepository.saveAll(List.of(
        momentNotification, likeNotification, commentNotification, readNotification));

    List<NotificationType> momentTypes = List.of(
        NotificationType.NEW_COMMENT_ON_MOMENT, NotificationType.MOMENT_LIKED);

    // when
    List<Notification> result = notificationRepository
        .findAllByUserIdAndIsReadAndNotificationTypeIn(userId, false, momentTypes);

    // then
    assertThat(result).hasSize(2);
}
```

---

## Task 8.4: NotificationServiceTest 수정

### 파일
- 수정: `src/test/java/moment/notification/service/notification/NotificationServiceTest.java`

### 변경 범위

**모든 `new Notification()` 호출과 `notificationService.save()` 호출 변경**

```java
// AS-IS
notificationService.save(user, contentId, reason, contentType, null)

// TO-BE
notificationService.save(user, NotificationType.NEW_COMMENT_ON_MOMENT,
    SourceData.of(Map.of("momentId", contentId)), "/moments/" + contentId)
```

**삭제할 테스트**:
- `읽지_않은_알림의_타겟_ID들을_조회한다()` — `getUnreadTargetIdsBy` 삭제됨
- `타겟_ID와_읽음_여부로_알림_목록을_조회한다()` — `getNotificationsBy` 삭제됨

**추가할 테스트**:

```java
@Test
void notification_type_목록으로_사용자의_알림을_조회한다() {
    // given
    notificationRepository.save(new Notification(user,
        NotificationType.NEW_COMMENT_ON_MOMENT,
        SourceData.of(Map.of("momentId", 1L)), "/moments/1"));
    notificationRepository.save(new Notification(user,
        NotificationType.COMMENT_LIKED,
        SourceData.of(Map.of("commentId", 2L)), "/comments/2"));

    List<NotificationType> momentTypes = List.of(
        NotificationType.NEW_COMMENT_ON_MOMENT, NotificationType.MOMENT_LIKED);

    // when
    List<Notification> result = notificationService.getAllBy(user.getId(), false, momentTypes);

    // then
    assertThat(result).hasSize(1);
}
```

---

## Task 8.5: NotificationApplicationServiceTest 수정

### 파일
- 수정: `src/test/java/moment/notification/service/application/NotificationApplicationServiceTest.java`

### 변경 범위

**모든 `new Notification()` 호출 변경**

**삭제할 테스트**:
- `읽지_않은_알림의_타겟_ID_목록을_조회한다()` — `getUnreadNotifications(userId, TargetType)` 삭제됨
- `타겟_ID와_Type으로_읽지_않은_알림을_조회하고_맵으로_반환한다()` — `getNotificationsByTargetIdsAndTargetType` 삭제됨

**추가할 테스트**:

```java
@Test
void 읽지_않은_모멘트_ID_목록을_조회한다() {
    notificationRepository.save(new Notification(user,
        NotificationType.NEW_COMMENT_ON_MOMENT,
        SourceData.of(Map.of("momentId", 10L)), "/moments/10"));
    notificationRepository.save(new Notification(user,
        NotificationType.MOMENT_LIKED,
        SourceData.of(Map.of("momentId", 20L)), "/moments/20"));
    notificationRepository.save(new Notification(user,
        NotificationType.COMMENT_LIKED,
        SourceData.of(Map.of("commentId", 30L)), "/comments/30"));

    List<Long> result = notificationApplicationService.getUnreadMomentIds(user.getId());

    assertThat(result).containsExactlyInAnyOrder(10L, 20L);
}

@Test
void 읽지_않은_코멘트_ID_목록을_조회한다() {
    notificationRepository.save(new Notification(user,
        NotificationType.COMMENT_LIKED,
        SourceData.of(Map.of("commentId", 15L)), "/comments/15"));
    notificationRepository.save(new Notification(user,
        NotificationType.NEW_COMMENT_ON_MOMENT,
        SourceData.of(Map.of("momentId", 10L)), "/moments/10"));

    List<Long> result = notificationApplicationService.getUnreadCommentIds(user.getId());

    assertThat(result).containsExactly(15L);
}
```

---

## Task 8.6: NotificationFacadeServiceTest 수정

### 파일
- 수정: `src/test/java/moment/notification/service/facade/NotificationFacadeServiceTest.java`

### 변경

```java
// AS-IS
notificationFacadeService.notify(new NotificationCommand(
    userId, contentId, reason, contentType,
    null, PushNotificationMessage.REPLY_TO_MOMENT));

// TO-BE
notificationFacadeService.notify(new NotificationCommand(
    userId, NotificationType.NEW_COMMENT_ON_MOMENT,
    SourceData.of(Map.of("momentId", contentId)),
    PushNotificationMessage.REPLY_TO_MOMENT));
```

Import 변경:
- 삭제: `import moment.global.domain.TargetType;`
- 추가: `import moment.notification.domain.SourceData;`, `import java.util.Map;`

---

## Task 8.7: NotificationEventHandlerTest 수정

### 파일
- 수정: `src/test/java/moment/notification/service/eventHandler/NotificationEventHandlerTest.java`

### 변경

모든 `verify` 호출에서 `NotificationCommand` 변경:

```java
// AS-IS
verify(notificationFacadeService).notify(new NotificationCommand(
    2L, 1L, NotificationType.GROUP_JOIN_REQUEST, TargetType.GROUP,
    1L, PushNotificationMessage.GROUP_JOIN_REQUEST));

// TO-BE
verify(notificationFacadeService).notify(new NotificationCommand(
    2L, NotificationType.GROUP_JOIN_REQUEST,
    SourceData.of(Map.of("groupId", 1L)),
    PushNotificationMessage.GROUP_JOIN_REQUEST));
```

> **주의**: `SourceData`는 record이므로 `equals()` 비교에 `Map` 내용이 포함됨.
> `Map.of("groupId", 1L)`와 실제 핸들러가 생성하는 Map이 동일해야 verify 통과.

---

## Task 8.8: ExpoPushNotificationSenderTest 수정

### 파일
- 수정: `src/test/java/moment/notification/infrastructure/expo/ExpoPushNotificationSenderTest.java`

### 변경

모든 `PushNotificationCommand` 생성자 변경:

```java
// AS-IS
PushNotificationCommand command = new PushNotificationCommand(user,
    PushNotificationMessage.REPLY_TO_MOMENT);

// TO-BE
PushNotificationCommand command = new PushNotificationCommand(user,
    PushNotificationMessage.REPLY_TO_MOMENT, "/moments/42");
```

**추가 테스트**:

```java
@Test
void link가_null이면_data에_link를_포함하지_않는다() {
    PushNotificationCommand command = new PushNotificationCommand(user,
        PushNotificationMessage.GROUP_KICKED, null);
    PushNotification pushNotification = new PushNotification(user, "ExponentPushToken[xxx]");

    when(pushNotificationRepository.findByUserId(user.getId()))
        .thenReturn(List.of(pushNotification));
    when(expoPushApiClient.send(anyList()))
        .thenReturn(List.of(new ExpoPushTicketResponse("ticket-1", "ok", null, null)));

    expoPushNotificationSender.send(command);

    verify(expoPushApiClient).send(argThat(messages -> {
        ExpoPushMessage message = messages.get(0);
        return message.data().isEmpty();
    }));
}

@Test
void link가_있으면_data에_link를_포함한다() {
    PushNotificationCommand command = new PushNotificationCommand(user,
        PushNotificationMessage.REPLY_TO_MOMENT, "/moments/42");
    PushNotification pushNotification = new PushNotification(user, "ExponentPushToken[xxx]");

    when(pushNotificationRepository.findByUserId(user.getId()))
        .thenReturn(List.of(pushNotification));
    when(expoPushApiClient.send(anyList()))
        .thenReturn(List.of(new ExpoPushTicketResponse("ticket-1", "ok", null, null)));

    expoPushNotificationSender.send(command);

    verify(expoPushApiClient).send(argThat(messages -> {
        ExpoPushMessage message = messages.get(0);
        return "/moments/42".equals(message.data().get("link"));
    }));
}
```

---

## 검증 절차

### 1. 단위 테스트 실행

```bash
./gradlew fastTest
```

### 2. TargetType 참조 확인

```bash
# notification 패키지 내 TargetType 참조가 없어야 함
grep -r "TargetType" src/main/java/moment/notification/ || echo "Clean!"
grep -r "TargetType" src/test/java/moment/notification/ || echo "Clean!"
```

### 3. deepLink 참조 확인

```bash
# notification 패키지 내 deepLink 참조가 없어야 함 (link로 통일)
grep -r "deepLink" src/main/java/moment/notification/ || echo "Clean!"
grep -r "deepLink" src/test/java/moment/notification/ || echo "Clean!"
```

---

## 완료 조건

- [ ] `NotificationPayloadTest` 삭제
- [ ] `NotificationResponseTest` 수정
- [ ] `NotificationRepositoryTest` 수정 (TargetType 쿼리 테스트 삭제, NotificationType 테스트 추가)
- [ ] `NotificationServiceTest` 수정
- [ ] `NotificationApplicationServiceTest` 수정
- [ ] `NotificationFacadeServiceTest` 수정
- [ ] `NotificationEventHandlerTest` 수정
- [ ] `ExpoPushNotificationSenderTest` 수정
- [ ] `./gradlew fastTest` 전체 통과
- [ ] notification 패키지 내 `TargetType` 참조 0건
- [ ] notification 패키지 내 `deepLink` 참조 0건
