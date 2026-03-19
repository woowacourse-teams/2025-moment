# Phase 2: 좋아요 이벤트에 groupId 추가

> Parent: [plan.md](./plan.md)
> Status: 구현 대기
> 선행 조건: Phase 1 완료
> 대상 파일: `MomentLikeEvent.java`, `CommentLikeEvent.java`, `MomentLikeService.java`, `CommentLikeService.java`, `MomentLikeServiceTest.java`, `CommentLikeServiceTest.java`

## 목표

MomentLikeEvent, CommentLikeEvent record에 groupId 필드를 추가하고,
이벤트 발행부(Service)에서 groupId를 포함하도록 수정한다.

---

## Step 2-1: MomentLikeEvent에 groupId 필드 추가

**파일**: `src/main/java/moment/like/dto/event/MomentLikeEvent.java`

```java
// 현재
public record MomentLikeEvent(
    Long momentId,
    Long momentOwnerId,
    Long likeMemberId,
    String likerNickname
) {}

// 수정 후
public record MomentLikeEvent(
    Long momentId,
    Long momentOwnerId,
    Long likeMemberId,
    String likerNickname,
    Long groupId
) {}
```

> 이 수정 직후 컴파일 에러 발생 지점:
> - `MomentLikeService.java:42` - 생성자 호출 (인자 4개 → 5개 필요)
> - `NotificationEventHandlerTest.java:103` - 테스트에서 생성자 호출 (인자 4개 → 5개 필요)

---

## Step 2-2: MomentLikeService 이벤트 발행부 수정

**파일**: `src/main/java/moment/like/service/MomentLikeService.java:42-47`

```java
// 현재
eventPublisher.publishEvent(new MomentLikeEvent(
    moment.getId(),
    moment.getMomenter().getId(),
    member.getId(),
    member.getNickname()
));

// 수정 후
eventPublisher.publishEvent(new MomentLikeEvent(
    moment.getId(),
    moment.getMomenter().getId(),
    member.getId(),
    member.getNickname(),
    moment.getGroup().getId()
));
```

**groupId 획득 경로**: `moment.getGroup().getId()`
- Moment 엔티티의 `group` 필드 (`@ManyToOne(fetch = FetchType.LAZY)`)
- 좋아요는 그룹 컨텍스트에서만 발생하므로 group은 항상 non-null

---

## Step 2-3: MomentLikeServiceTest 컴파일 에러 해결

**파일**: `src/test/java/moment/like/service/MomentLikeServiceTest.java`

기존 테스트는 `verify(eventPublisher).publishEvent(any(MomentLikeEvent.class))` (line 129)로 이벤트 발행만 검증하므로, record 필드 추가로 인한 컴파일 에러는 발생하지 않음.

> `any(MomentLikeEvent.class)`는 타입만 검사하므로 필드 추가와 무관하게 통과.
> MomentLikeServiceTest 자체에는 수정 불필요.

**실행**: `./gradlew test --tests "moment.like.service.MomentLikeServiceTest"` → ALL PASS 확인

---

## Step 2-4: CommentLikeEvent에 groupId 필드 추가

**파일**: `src/main/java/moment/like/dto/event/CommentLikeEvent.java`

```java
// 현재
public record CommentLikeEvent(
    Long commentId,
    Long commentOwnerId,
    Long likeMemberId,
    String likerNickname
) {}

// 수정 후
public record CommentLikeEvent(
    Long commentId,
    Long commentOwnerId,
    Long likeMemberId,
    String likerNickname,
    Long groupId
) {}
```

> 컴파일 에러 발생 지점:
> - `CommentLikeService.java:48` - 생성자 호출 (인자 4개 → 5개 필요)
> - `NotificationEventHandlerTest.java:119` - 테스트에서 생성자 호출 (인자 4개 → 5개 필요)

---

## Step 2-5: CommentLikeService 이벤트 발행부 수정

**파일**: `src/main/java/moment/like/service/CommentLikeService.java:48-53`

```java
// 현재
eventPublisher.publishEvent(new CommentLikeEvent(
    comment.getId(),
    comment.getCommenter().getId(),
    member.getId(),
    member.getNickname()
));

// 수정 후
eventPublisher.publishEvent(new CommentLikeEvent(
    comment.getId(),
    comment.getCommenter().getId(),
    member.getId(),
    member.getNickname(),
    member.getGroup().getId()
));
```

**groupId 획득 경로**: `member.getGroup().getId()`
- Comment 엔티티에는 group 필드 없음
- GroupMember의 `group` 필드에서 획득 (`@ManyToOne(fetch = FetchType.LAZY)`)
- toggle 메서드의 두 번째 파라미터 `GroupMember member`에서 접근

---

## Step 2-6: CommentLikeServiceTest 컴파일 에러 해결

**파일**: `src/test/java/moment/like/service/CommentLikeServiceTest.java`

MomentLikeServiceTest와 동일하게, `verify(eventPublisher).publishEvent(any(CommentLikeEvent.class))` (line 139)로 이벤트 발행만 검증하므로 수정 불필요.

**실행**: `./gradlew test --tests "moment.like.service.CommentLikeServiceTest"` → ALL PASS 확인

---

## Step 2-7: 전체 like 서비스 테스트 검증

**실행**: `./gradlew test --tests "moment.like.service.*"` → ALL PASS 확인

---

## 최종 결과물

### 수정된 파일 (4개)

| 파일 | 변경 내용 |
|------|----------|
| `MomentLikeEvent.java` | `Long groupId` 필드 추가 |
| `CommentLikeEvent.java` | `Long groupId` 필드 추가 |
| `MomentLikeService.java:42-47` | `moment.getGroup().getId()` 추가 |
| `CommentLikeService.java:48-53` | `member.getGroup().getId()` 추가 |

### 수정 불필요 파일 (2개)

| 파일 | 이유 |
|------|------|
| `MomentLikeServiceTest.java` | `any(MomentLikeEvent.class)`로 검증하므로 필드 추가 무관 |
| `CommentLikeServiceTest.java` | `any(CommentLikeEvent.class)`로 검증하므로 필드 추가 무관 |

### groupId 획득 경로 요약

| 이벤트 | 서비스 메서드 | groupId 획득 |
|--------|-------------|-------------|
| MomentLikeEvent | `MomentLikeService.toggle(Moment, GroupMember)` | `moment.getGroup().getId()` |
| CommentLikeEvent | `CommentLikeService.toggle(Comment, GroupMember)` | `member.getGroup().getId()` |
