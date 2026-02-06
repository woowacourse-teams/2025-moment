# Phase 2: Notification 엔티티 변경 + Flyway 마이그레이션

> 의존성: Phase 1 완료
> 예상 파일: 2개 마이그레이션 + 1개 엔티티 수정 + 2개 테스트 수정

## 목표

`Notification` 엔티티에서 `targetType`, `targetId`, `groupId` 필드를 제거하고
`sourceData` (SourceData) + `link` (String) 필드를 추가한다.
Flyway 마이그레이션으로 DB 스키마를 변경한다.

---

## Task 2.1: Flyway V36 마이그레이션 스크립트 (MySQL)

### 파일
- 신규: `src/main/resources/db/migration/mysql/V36__alter_notifications_remove_legacy_add_source_and_link.sql`

### 내용

```sql
-- 1. 기존 알림 데이터 전량 삭제
DELETE FROM notifications;

-- 2. 기존 컬럼 삭제
ALTER TABLE notifications DROP COLUMN target_type;
ALTER TABLE notifications DROP COLUMN target_id;
ALTER TABLE notifications DROP INDEX idx_notifications_group;
ALTER TABLE notifications DROP COLUMN group_id;

-- 3. 신규 컬럼 추가
ALTER TABLE notifications ADD COLUMN source_data JSON DEFAULT NULL;
ALTER TABLE notifications ADD COLUMN link VARCHAR(512) DEFAULT NULL;
```

---

## Task 2.2: Flyway V36 마이그레이션 스크립트 (H2 - 테스트)

### 파일
- 신규: `src/test/resources/db/migration/h2/V36__alter_notifications_remove_legacy_add_source_and_link__h2.sql`

### 내용

```sql
-- H2에서는 JSON 타입 대신 TEXT 사용
DELETE FROM notifications;

ALTER TABLE notifications DROP COLUMN target_type;
ALTER TABLE notifications DROP COLUMN target_id;
DROP INDEX IF EXISTS idx_notifications_group;
ALTER TABLE notifications DROP COLUMN group_id;

ALTER TABLE notifications ADD COLUMN source_data TEXT DEFAULT NULL;
ALTER TABLE notifications ADD COLUMN link VARCHAR(512) DEFAULT NULL;
```

> **주의**: H2에서는 `DROP INDEX` 문법이 MySQL과 다름.
> H2에서 `JSON` 타입을 지원하지만 JPA `@Convert`를 사용하므로 `TEXT`로도 충분.

---

## Task 2.3: Notification 엔티티 변경

### 파일
- 수정: `src/main/java/moment/notification/domain/Notification.java`

### AS-IS 필드 (삭제 대상)

```java
@Enumerated(EnumType.STRING)
private TargetType targetType;         // 삭제

private Long targetId;                  // 삭제

@Column(name = "group_id")
private Long groupId;                   // 삭제
```

### AS-IS 생성자 (삭제 대상)

```java
// 생성자 1: 삭제
public Notification(User user, NotificationType notificationType, TargetType targetType, Long targetId)

// 생성자 2: 삭제
public Notification(User user, NotificationType notificationType, TargetType targetType, Long targetId, Long groupId)
```

### TO-BE 필드 (추가)

```java
@Column(name = "source_data", columnDefinition = "JSON")
@Convert(converter = SourceDataConverter.class)
private SourceData sourceData;

@Column(name = "link", length = 512)
private String link;
```

### TO-BE 생성자

```java
public Notification(User user,
                    NotificationType notificationType,
                    SourceData sourceData,
                    String link) {
    this.user = user;
    this.notificationType = notificationType;
    this.sourceData = sourceData;
    this.link = link;
    this.isRead = false;
}
```

### Import 변경
- 삭제: `import moment.global.domain.TargetType;`
- 추가: `import moment.notification.infrastructure.SourceDataConverter;`

---

## Task 2.4: NotificationTest 수정

### 파일
- 수정: `src/test/java/moment/notification/domain/NotificationTest.java`

### AS-IS 테스트 (삭제)

```java
void 알림_객체를_읽으면_참이_된다()  // 생성자 변경으로 수정 필요
void groupId를_포함하여_알림_객체를_생성한다()  // targetType, targetId, groupId 제거
```

### TO-BE 테스트

```java
@Test
void 알림_객체를_읽으면_참이_된다() {
    User user = UserFixture.createUser();
    SourceData sourceData = SourceData.of(Map.of("momentId", 42L));
    Notification notification = new Notification(
        user, NotificationType.NEW_COMMENT_ON_MOMENT, sourceData, "/moments/42");

    notification.markAsRead();

    assertThat(notification.isRead()).isTrue();
}

@Test
void sourceData와_link를_포함하여_알림_객체를_생성한다() {
    User user = UserFixture.createUser();
    SourceData sourceData = SourceData.of(Map.of("groupId", 3L));
    String link = "/groups/3";

    Notification notification = new Notification(
        user, NotificationType.GROUP_JOIN_REQUEST, sourceData, link);

    assertThat(notification.getSourceData()).isEqualTo(sourceData);
    assertThat(notification.getLink()).isEqualTo(link);
    assertThat(notification.getNotificationType()).isEqualTo(NotificationType.GROUP_JOIN_REQUEST);
    assertThat(notification.isRead()).isFalse();
}

@Test
void GROUP_KICKED_알림은_link가_null이다() {
    User user = UserFixture.createUser();
    SourceData sourceData = SourceData.of(Map.of("groupId", 3L));

    Notification notification = new Notification(
        user, NotificationType.GROUP_KICKED, sourceData, null);

    assertThat(notification.getLink()).isNull();
}
```

---

## ⚠️ 주의사항: 빌드 깨짐 구간

이 Phase를 적용하면 **기존 생성자를 사용하는 모든 코드가 컴파일 에러**를 발생시킴.
따라서 Phase 2 완료 직후 **Phase 3~7을 연속 진행**해야 빌드가 복구됨.

**컴파일 에러 발생 위치** (Phase 3~7에서 순차 수정):
- `NotificationService.save()` — Phase 3
- `NotificationApplicationService.createNotification()` — Phase 3
- `NotificationFacadeService.notify()` — Phase 3
- `NotificationEventHandler` (7개 핸들러) — Phase 4
- `NotificationRepository` (TargetType 쿼리) — Phase 3
- `NotificationResponse.from()` — Phase 6
- `NotificationSseResponse.of()` — Phase 6
- `NotificationPayload.from()` — Phase 6 (삭제)
- 모든 테스트 파일 (Notification 생성자 사용) — Phase 8

### 빌드 깨짐 최소화 전략

**옵션 A (권장)**: Phase 2~7을 하나의 작업 세션에서 연속 수행. 중간 커밋 없이 전체 완료 후 커밋.

**옵션 B**: Phase 2에서 기존 생성자를 `@Deprecated`로 유지하고, Phase 3~7 완료 후 삭제.
→ 스펙에서 하위 호환 제거를 명시했으므로 **옵션 A 권장**.

---

## 완료 조건

- [ ] V36 MySQL 마이그레이션 스크립트 작성
- [ ] V36 H2 마이그레이션 스크립트 작성
- [ ] Notification 엔티티에서 `targetType`, `targetId`, `groupId` 제거
- [ ] Notification 엔티티에 `sourceData`, `link` 추가
- [ ] NotificationTest 수정
- [ ] ⚠️ 이 시점에서는 빌드 실패 — Phase 3~7 연속 진행 필요
