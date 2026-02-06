# Phase 7: 외부 도메인 의존성 변경

> 의존성: Phase 3 완료 (NotificationApplicationService 새 메서드 존재)
> 예상 파일: 2개 프로덕션 수정

## 목표

`MyGroupMomentPageFacadeService`와 `MyGroupCommentPageFacadeService`에서
`NotificationApplicationService`의 TargetType 기반 호출을 새 메서드로 교체한다.

---

## Task 7.1: MyGroupMomentPageFacadeService 변경

### 파일
- 수정: `src/main/java/moment/moment/service/facade/MyGroupMomentPageFacadeService.java`

### AS-IS (line 60-61)

```java
List<Long> unreadMomentIds = notificationApplicationService.getUnreadNotifications(
    userId, TargetType.MOMENT);
```

### TO-BE

```java
List<Long> unreadMomentIds = notificationApplicationService.getUnreadMomentIds(userId);
```

### AS-IS (line 86-88)

```java
Map<Long, List<Long>> notificationsMap =
    notificationApplicationService.getNotificationsByTargetIdsAndTargetType(
        momentIds, TargetType.MOMENT);
```

### TO-BE

```java
Map<Long, List<Long>> notificationsMap =
    notificationApplicationService.getNotificationsByMomentIds(momentIds);
```

### Import 변경

```diff
- import moment.global.domain.TargetType;
```

---

## Task 7.2: MyGroupCommentPageFacadeService 변경

### 파일
- 수정: `src/main/java/moment/comment/service/facade/MyGroupCommentPageFacadeService.java`

### AS-IS (line 56-57)

```java
List<Long> unreadCommentIds = notificationApplicationService.getUnreadNotifications(
    userId, TargetType.COMMENT);
```

### TO-BE

```java
List<Long> unreadCommentIds = notificationApplicationService.getUnreadCommentIds(userId);
```

### AS-IS (line 88-90)

```java
Map<Long, List<Long>> notificationsMap =
    notificationApplicationService.getNotificationsByTargetIdsAndTargetType(
        commentIds, TargetType.COMMENT);
```

### TO-BE

```java
Map<Long, List<Long>> notificationsMap =
    notificationApplicationService.getNotificationsByCommentIds(commentIds);
```

### Import 변경

```diff
- import moment.global.domain.TargetType;
```

---

## 영향도 분석

| 변경 전 메서드 | 변경 후 메서드 | 호출 위치 |
|---------------|--------------|----------|
| `getUnreadNotifications(userId, TargetType.MOMENT)` | `getUnreadMomentIds(userId)` | MyGroupMomentPageFacadeService:60 |
| `getNotificationsByTargetIdsAndTargetType(ids, TargetType.MOMENT)` | `getNotificationsByMomentIds(ids)` | MyGroupMomentPageFacadeService:86 |
| `getUnreadNotifications(userId, TargetType.COMMENT)` | `getUnreadCommentIds(userId)` | MyGroupCommentPageFacadeService:56 |
| `getNotificationsByTargetIdsAndTargetType(ids, TargetType.COMMENT)` | `getNotificationsByCommentIds(ids)` | MyGroupCommentPageFacadeService:88 |

### 반환 타입 호환성

| 메서드 | 기존 반환 타입 | 새 반환 타입 | 호환 |
|--------|-------------|------------|------|
| `getUnreadMomentIds` | `List<Long>` | `List<Long>` | ✅ 동일 |
| `getUnreadCommentIds` | `List<Long>` | `List<Long>` | ✅ 동일 |
| `getNotificationsByMomentIds` | `Map<Long, List<Long>>` | `Map<Long, List<Long>>` | ✅ 동일 |
| `getNotificationsByCommentIds` | `Map<Long, List<Long>>` | `Map<Long, List<Long>>` | ✅ 동일 |

반환 타입이 동일하므로 호출부의 나머지 코드 수정 불필요.

---

## 완료 조건

- [ ] `MyGroupMomentPageFacadeService`에서 TargetType 참조 제거
- [ ] `MyGroupCommentPageFacadeService`에서 TargetType 참조 제거
- [ ] 두 파일 모두 `TargetType` import 삭제
- [ ] 이 시점에서 notification 도메인 + 외부 호출부에서 TargetType 사용 완전 제거
