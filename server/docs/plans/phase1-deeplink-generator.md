# Phase 1: DeepLinkGenerator 딥링크 경로 수정

> Parent: [plan.md](./plan.md)
> Status: 구현 대기
> 대상 파일: `DeepLinkGenerator.java`, `DeepLinkGeneratorTest.java`

## 목표

DeepLinkGenerator가 생성하는 모든 딥링크 경로를 클라이언트 라우트에 맞게 수정한다.
개인 모멘트(group == null)는 레거시 기능으로 제외한다.

---

## Step 1-1: 그룹 모멘트 댓글 딥링크 테스트 수정 (RED)

**파일**: `src/test/java/moment/notification/domain/DeepLinkGeneratorTest.java:27-34`

```java
// 현재 (line 33)
assertThat(link).isEqualTo("/groups/3/moments/42");

// 수정 후
assertThat(link).isEqualTo("/groups/3/collection/my-moment");
```

**실행**: `./gradlew test --tests "moment.notification.domain.DeepLinkGeneratorTest.그룹_모멘트_댓글_알림의_딥링크를_생성한다"` → FAIL 확인

---

## Step 1-2: 구현 (GREEN)

**파일**: `src/main/java/moment/notification/domain/DeepLinkGenerator.java:7-13`

```java
// 현재
case NEW_COMMENT_ON_MOMENT -> {
    Long groupId = sourceData.getLong("groupId");
    Long momentId = sourceData.getLong("momentId");
    yield (groupId != null)
        ? "/groups/" + groupId + "/moments/" + momentId
        : "/moments/" + momentId;
}

// 수정 후
case NEW_COMMENT_ON_MOMENT, MOMENT_LIKED ->
    "/groups/" + sourceData.getLong("groupId") + "/collection/my-moment";
```

> 주의: 이 시점에서 MOMENT_LIKED도 함께 수정해야 switch가 컴파일됨 (중복 case 제거).
> 기존 MOMENT_LIKED case (line 17-18)는 삭제.

**실행**: 같은 테스트 → PASS 확인

---

## Step 1-3: 그룹 가입 신청 딥링크 테스트 수정 (RED)

**파일**: `DeepLinkGeneratorTest.java:43`

```java
// 현재
assertThat(link).isEqualTo("/groups/3");

// 수정 후
assertThat(link).isEqualTo("/groups/3/today-moment");
```

**실행**: `./gradlew test --tests "moment.notification.domain.DeepLinkGeneratorTest.그룹_가입_신청_알림의_딥링크를_생성한다"` → FAIL 확인

---

## Step 1-4: 구현 (GREEN)

**파일**: `DeepLinkGenerator.java:14-15`

```java
// 현재
case GROUP_JOIN_REQUEST, GROUP_JOIN_APPROVED ->
    "/groups/" + sourceData.getLong("groupId");

// 수정 후
case GROUP_JOIN_REQUEST, GROUP_JOIN_APPROVED ->
    "/groups/" + sourceData.getLong("groupId") + "/today-moment";
```

**실행**: 같은 테스트 → PASS 확인

---

## Step 1-5: 그룹 가입 승인 딥링크 테스트 수정 (RED)

**파일**: `DeepLinkGeneratorTest.java:53`

```java
// 현재
assertThat(link).isEqualTo("/groups/3");

// 수정 후
assertThat(link).isEqualTo("/groups/3/today-moment");
```

**실행**: `./gradlew test --tests "moment.notification.domain.DeepLinkGeneratorTest.그룹_가입_승인_알림의_딥링크를_생성한다"` → FAIL 확인

> Step 1-4에서 이미 구현이 되어 있으므로 바로 PASS될 수 있음. 그래도 테스트 기대값은 수정해야 함.

---

## Step 1-6: 모멘트 좋아요 딥링크 테스트 수정 (RED)

**파일**: `DeepLinkGeneratorTest.java:67-74`

```java
// 현재
@Test
void 모멘트_좋아요_알림의_딥링크를_생성한다() {
    SourceData sourceData = SourceData.of(Map.of("momentId", 42L));

    String link = DeepLinkGenerator.generate(
        NotificationType.MOMENT_LIKED, sourceData);

    assertThat(link).isEqualTo("/moments/42");
}

// 수정 후 (SourceData에 groupId 추가 + 기대값 변경)
@Test
void 모멘트_좋아요_알림의_딥링크를_생성한다() {
    SourceData sourceData = SourceData.of(Map.of("momentId", 42L, "groupId", 3L));

    String link = DeepLinkGenerator.generate(
        NotificationType.MOMENT_LIKED, sourceData);

    assertThat(link).isEqualTo("/groups/3/collection/my-moment");
}
```

**실행**: `./gradlew test --tests "moment.notification.domain.DeepLinkGeneratorTest.모멘트_좋아요_알림의_딥링크를_생성한다"` → PASS (Step 1-2에서 이미 구현됨)

---

## Step 1-7: 코멘트 좋아요 딥링크 테스트 수정 (RED)

**파일**: `DeepLinkGeneratorTest.java:77-84`

```java
// 현재
@Test
void 코멘트_좋아요_알림의_딥링크를_생성한다() {
    SourceData sourceData = SourceData.of(Map.of("commentId", 15L));

    String link = DeepLinkGenerator.generate(
        NotificationType.COMMENT_LIKED, sourceData);

    assertThat(link).isEqualTo("/comments/15");
}

// 수정 후 (SourceData에 groupId 추가 + 기대값 변경)
@Test
void 코멘트_좋아요_알림의_딥링크를_생성한다() {
    SourceData sourceData = SourceData.of(Map.of("commentId", 15L, "groupId", 3L));

    String link = DeepLinkGenerator.generate(
        NotificationType.COMMENT_LIKED, sourceData);

    assertThat(link).isEqualTo("/groups/3/collection/my-comment");
}
```

**실행**: `./gradlew test --tests "moment.notification.domain.DeepLinkGeneratorTest.코멘트_좋아요_알림의_딥링크를_생성한다"` → FAIL

---

## Step 1-8: 구현 (GREEN)

**파일**: `DeepLinkGenerator.java:19-20`

```java
// 현재
case COMMENT_LIKED ->
    "/comments/" + sourceData.getLong("commentId");

// 수정 후
case COMMENT_LIKED ->
    "/groups/" + sourceData.getLong("groupId") + "/collection/my-comment";
```

**실행**: 같은 테스트 → PASS 확인

---

## Step 1-9: 개인 모멘트 댓글 테스트 삭제

**파일**: `DeepLinkGeneratorTest.java:17-24`

개인 모멘트는 레거시이므로 해당 테스트 삭제:

```java
// 삭제 대상
@Test
void 개인_모멘트_댓글_알림의_딥링크를_생성한다() {
    SourceData sourceData = SourceData.of(Map.of("momentId", 42L));

    String link = DeepLinkGenerator.generate(
        NotificationType.NEW_COMMENT_ON_MOMENT, sourceData);

    assertThat(link).isEqualTo("/moments/42");
}
```

---

## Step 1-10: 전체 테스트 검증

**실행**: `./gradlew test --tests "moment.notification.domain.DeepLinkGeneratorTest"` → ALL PASS

---

## 최종 결과물

### DeepLinkGenerator.java (수정 후 전체)

```java
public class DeepLinkGenerator {

    public static String generate(NotificationType notificationType, SourceData sourceData) {
        return switch (notificationType) {
            case NEW_COMMENT_ON_MOMENT, MOMENT_LIKED ->
                "/groups/" + sourceData.getLong("groupId") + "/collection/my-moment";
            case GROUP_JOIN_REQUEST, GROUP_JOIN_APPROVED ->
                "/groups/" + sourceData.getLong("groupId") + "/today-moment";
            case GROUP_KICKED -> null;
            case COMMENT_LIKED ->
                "/groups/" + sourceData.getLong("groupId") + "/collection/my-comment";
        };
    }
}
```

### DeepLinkGeneratorTest.java (수정 후 테스트 6개)

| # | 테스트명 | SourceData | 기대값 |
|---|---------|------------|--------|
| 1 | 그룹_모멘트_댓글_알림의_딥링크를_생성한다 | `{momentId:42, groupId:3}` | `/groups/3/collection/my-moment` |
| 2 | 그룹_가입_신청_알림의_딥링크를_생성한다 | `{groupId:3}` | `/groups/3/today-moment` |
| 3 | 그룹_가입_승인_알림의_딥링크를_생성한다 | `{groupId:3}` | `/groups/3/today-moment` |
| 4 | 그룹_강퇴_알림의_딥링크는_null이다 | `{groupId:3}` | `null` |
| 5 | 모멘트_좋아요_알림의_딥링크를_생성한다 | `{momentId:42, groupId:3}` | `/groups/3/collection/my-moment` |
| 6 | 코멘트_좋아요_알림의_딥링크를_생성한다 | `{commentId:15, groupId:3}` | `/groups/3/collection/my-comment` |
