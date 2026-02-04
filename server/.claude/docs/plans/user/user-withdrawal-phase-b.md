# Phase B: 콘텐츠 표시 수정 세부 구현 계획

> **상위 계획**: `user-withdrawal.md`
> **선행 조건**: Phase A 완료 (회원 탈퇴 API)
> **목표**: 탈퇴 유저의 콘텐츠(모멘트, 댓글)를 유지하면서 "탈퇴한 사용자"로 표시

---

## 핵심 원리

### `@SQLRestriction("deleted_at IS NULL")` 동작 방식
- User 엔티티에 `@SQLRestriction` 적용 → soft-deleted User는 JPA 조회 시 자동 필터링
- `JOIN FETCH m.momenter` (INNER JOIN) → momenter가 deleted이면 **Moment 자체가 조회 안 됨**
- `LEFT JOIN FETCH m.momenter` → momenter가 deleted이면 **Moment는 조회되고 momenter = null**

### 데이터 흐름
```
탈퇴 유저의 모멘트 조회 시:
  MomentRepository (LEFT JOIN FETCH m.momenter)
    → Moment 객체 반환 (momenter = null)
      → MomentComposition.of(moment, imageUrl)
        → momenter != null ? momenter.getNickname() : "탈퇴한 사용자"
          → API Response: { nickname: "탈퇴한 사용자" }
```

---

## TDD 테스트 순서

---

### B-1: MomentRepository - INNER JOIN -> LEFT JOIN 변경

**파일**: `src/main/java/moment/moment/infrastructure/MomentRepository.java`

**변경 대상**: 7개 쿼리의 `JOIN FETCH m.momenter` → `LEFT JOIN FETCH m.momenter`

| 라인 | 메서드 | 변경 |
|------|--------|------|
| 83 | `findAllWithMomenterByIds` | `JOIN FETCH m.momenter` → `LEFT JOIN FETCH m.momenter` |
| 91 | `findByGroupIdOrderByIdDesc` | `JOIN FETCH m.momenter` → `LEFT JOIN FETCH m.momenter` |
| 101 | `findByGroupIdAndIdLessThanOrderByIdDesc` | `JOIN FETCH m.momenter` → `LEFT JOIN FETCH m.momenter` |
| 114 | `findByGroupIdAndMemberIdOrderByIdDesc` | `JOIN FETCH m.momenter` → `LEFT JOIN FETCH m.momenter` |
| 127 | `findByGroupIdAndMemberIdAndIdLessThanOrderByIdDesc` | `JOIN FETCH m.momenter` → `LEFT JOIN FETCH m.momenter` |
| 167 | `findByGroupIdAndMemberIdAndIdIn` | `JOIN FETCH m.momenter` → `LEFT JOIN FETCH m.momenter` |
| 183 | `findByGroupIdAndMemberIdAndIdInAndIdLessThan` | `JOIN FETCH m.momenter` → `LEFT JOIN FETCH m.momenter` |

**변경하지 않는 쿼리** (4개):
| 라인 | 메서드 | 이유 |
|------|--------|------|
| 21 | `findMyMomentFirstPage` | 개인 피드 - 탈퇴 유저는 인증 불가하므로 호출 불가 |
| 29 | `findMyUnreadMomentFirstPage` | 동일 |
| 37 | `findMyMomentsNextPage` | 동일 |
| 48 | `findMyUnreadMomentNextPage` | 동일 |

**예시 변경**:
```
변경 전 (line 80-86):
    @Query("""
          SELECT m
          FROM moments m
          JOIN FETCH m.momenter
          WHERE m.id IN :momentIds
           """)
    List<Moment> findAllWithMomenterByIds(@Param("momentIds")List<Long> momentIds);

변경 후:
    @Query("""
          SELECT m
          FROM moments m
          LEFT JOIN FETCH m.momenter
          WHERE m.id IN :momentIds
           """)
    List<Moment> findAllWithMomenterByIds(@Param("momentIds")List<Long> momentIds);
```

**테스트**: 구조적 변경 - 기존 테스트가 그대로 통과해야 함. B-2 ~ B-6 변경 후 통합 검증.

---

### B-2: MomentComposition - null 안전 처리

**파일**: `src/main/java/moment/moment/dto/response/tobe/MomentComposition.java`

**현재 코드** (line 15-24):
```java
public static MomentComposition of(Moment moment, String imageUrl) {
    return new MomentComposition(
            moment.getId(),
            moment.getMomenter().getId(),          // NPE 위험
            moment.getContent(),
            moment.getMomenter().getNickname(),     // NPE 위험
            imageUrl,
            moment.getCreatedAt()
    );
}
```

**변경 후**:
```java
public static MomentComposition of(Moment moment, String imageUrl) {
    User momenter = moment.getMomenter();
    return new MomentComposition(
            moment.getId(),
            momenter != null ? momenter.getId() : null,
            moment.getContent(),
            momenter != null ? momenter.getNickname() : "탈퇴한 사용자",
            imageUrl,
            moment.getCreatedAt()
    );
}
```

**추가 import**:
```java
import moment.user.domain.User;
```

#### 테스트 (Red 먼저)

**파일 생성**: `src/test/java/moment/moment/dto/response/tobe/MomentCompositionTest.java`

```
테스트 1: momenter가_null인_경우_탈퇴한_사용자로_표시된다
  - Given: Moment 객체 (momenter = null, reflection으로 설정)
  - When: MomentComposition.of(moment, "imageUrl")
  - Then:
    - result.momenterId() == null
    - result.nickname() == "탈퇴한 사용자"

테스트 2: momenter가_존재하는_경우_닉네임이_정상_표시된다
  - Given: Moment 객체 (momenter = user with nickname "미미")
  - When: MomentComposition.of(moment, "imageUrl")
  - Then:
    - result.momenterId() == user.getId()
    - result.nickname() == "미미"
```

**Fixture 구성**: Moment의 momenter를 null로 설정하려면 reflection 또는 테스트 전용 Moment 생성 필요. `Moment` 엔티티의 `momenter` 필드에 대해 reflection으로 null 설정.

---

### B-3: CommentComposition - null 안전 처리

**파일**: `src/main/java/moment/comment/dto/tobe/CommentComposition.java`

**현재 코드** (line 15-26):
```java
public static CommentComposition of(Comment comment, User commenter, String imageUrl) {
    return new CommentComposition(
            comment.getId(),
            comment.getContent(),
            commenter.getNickname(),               // NPE 위험
            imageUrl,
            comment.getCreatedAt(),
            comment.getMomentId()
    );
}
```

**변경 후**:
```java
public static CommentComposition of(Comment comment, User commenter, String imageUrl) {
    return new CommentComposition(
            comment.getId(),
            comment.getContent(),
            commenter != null ? commenter.getNickname() : "탈퇴한 사용자",
            imageUrl,
            comment.getCreatedAt(),
            comment.getMomentId()
    );
}
```

#### 테스트 (Red 먼저)

**파일 생성**: `src/test/java/moment/comment/dto/tobe/CommentCompositionTest.java`

```
테스트 1: commenter가_null인_경우_탈퇴한_사용자로_표시된다
  - Given: Comment 객체, commenter = null
  - When: CommentComposition.of(comment, null, "imageUrl")
  - Then: result.nickname() == "탈퇴한 사용자"

테스트 2: commenter가_존재하는_경우_닉네임이_정상_표시된다
  - Given: Comment 객체, commenter = user with nickname "미미"
  - When: CommentComposition.of(comment, commenter, "imageUrl")
  - Then: result.nickname() == "미미"
```

**Fixture**: `CommentComposition.of()`의 `commenter` 파라미터는 외부에서 주입되므로 null 전달이 간단.

---

### B-4: CommentApplicationService - mapCommentersByComments null 처리

**파일**: `src/main/java/moment/comment/service/application/CommentApplicationService.java`

**현재 코드** (line 83-92):
```java
private Map<Comment, User> mapCommentersByComments(List<User> commenters, List<Comment> comments) {
    Map<Long, User> userById = commenters.stream()
            .collect(Collectors.toMap(User::getId, user -> user));

    return comments.stream()
            .collect(Collectors.toMap(
                    comment -> comment,
                    comment -> userById.get(comment.getCommenter().getId())  // null value → NPE
            ));
}
```

**문제점**:
- `Collectors.toMap()`은 **null value를 허용하지 않음** → `NullPointerException` 발생
- 탈퇴 유저의 코멘트가 있으면 `userById.get(commenterId)`가 null 반환
- `@SQLRestriction`에 의해 탈퇴 유저는 `userService.getAllBy(commenterIds)`에서 조회되지 않음

**변경 후**:
```java
private Map<Comment, User> mapCommentersByComments(List<User> commenters, List<Comment> comments) {
    Map<Long, User> userById = commenters.stream()
            .collect(Collectors.toMap(User::getId, user -> user));

    Map<Comment, User> result = new HashMap<>();
    for (Comment comment : comments) {
        result.put(comment, userById.get(comment.getCommenter().getId()));
    }
    return result;
}
```

**추가 import**:
```java
import java.util.HashMap;
```

**`comment.getCommenter().getId()` NPE 위험?**:
- Comment 엔티티의 `commenter` 필드는 `@ManyToOne(fetch = FetchType.LAZY)` + `@JoinColumn(nullable = false)`
- JPA lazy proxy는 FK 값(`commenter_id`)만으로 proxy 객체를 생성하므로 `getId()`는 DB 조회 없이 FK 반환
- 따라서 soft-deleted User도 proxy를 통해 `getId()` 호출 가능 → NPE 없음

#### 테스트

**기존 테스트 파일**: `CommentApplicationServiceTest`가 있다면 추가. 없으면 이 변경은 B-2, B-3의 null 처리와 함께 통합 검증.

이 메서드는 private이므로 직접 단위 테스트 불가. 대신 `getMyCommentCompositionsBy()` 호출 시 탈퇴 유저의 코멘트가 포함된 시나리오로 간접 검증.

---

### B-5: CommentableMomentResponse - null 안전 처리

**파일**: `src/main/java/moment/moment/dto/response/CommentableMomentResponse.java`

**현재 코드** (line 26-38):
```java
public static CommentableMomentResponse of(Moment moment, MomentImage momentImage) {
    String momentImageUrl = null;
    if (momentImage != null) {
        momentImageUrl = momentImage.getImageUrl();
    }

    return new CommentableMomentResponse(
            moment.getId(),
            moment.getMomenter().getNickname(),    // NPE 위험
            moment.getContent(),
            momentImageUrl,
            moment.getCreatedAt());
}
```

**변경 후**:
```java
public static CommentableMomentResponse of(Moment moment, MomentImage momentImage) {
    String momentImageUrl = null;
    if (momentImage != null) {
        momentImageUrl = momentImage.getImageUrl();
    }

    User momenter = moment.getMomenter();
    String nickname = momenter != null ? momenter.getNickname() : "탈퇴한 사용자";

    return new CommentableMomentResponse(
            moment.getId(),
            nickname,
            moment.getContent(),
            momentImageUrl,
            moment.getCreatedAt());
}
```

**추가 import**:
```java
import moment.user.domain.User;
```

#### 테스트

**파일 생성**: `src/test/java/moment/moment/dto/response/CommentableMomentResponseTest.java`

```
테스트 1: momenter가_null인_경우_탈퇴한_사용자로_표시된다
  - Given: Moment (momenter = null, reflection)
  - When: CommentableMomentResponse.of(moment, null)
  - Then: result.nickname() == "탈퇴한 사용자"

테스트 2: momenter가_존재하는_경우_닉네임이_정상_표시된다
  - Given: Moment (momenter = user with nickname)
  - When: CommentableMomentResponse.of(moment, null)
  - Then: result.nickname() == user.getNickname()
```

---

### B-6: Like 서비스 - null 안전 처리

#### B-6a: MomentLikeService

**파일**: `src/main/java/moment/like/service/MomentLikeService.java`

**현재 코드** (line 39-46):
```java
if (isNowLiked && !moment.getMomenter().getId().equals(member.getUser().getId())) {
    eventPublisher.publishEvent(new MomentLikeEvent(
        moment.getId(),
        moment.getMomenter().getId(),
        member.getId(),
        member.getNickname()
    ));
}
```

**변경 후**:
```java
User momenter = moment.getMomenter();
if (isNowLiked && momenter != null && !momenter.getId().equals(member.getUser().getId())) {
    eventPublisher.publishEvent(new MomentLikeEvent(
        moment.getId(),
        momenter.getId(),
        member.getId(),
        member.getNickname()
    ));
}
```

**추가 import**:
```java
import moment.user.domain.User;
```

**설계 근거**:
- momenter가 null (탈퇴 유저) → 알림 발송 skip (수신자 없음)
- NPE 방지

#### B-6b: CommentLikeService

**파일**: `src/main/java/moment/like/service/CommentLikeService.java`

**현재 코드** (line 45-52):
```java
if (isNowLiked && !comment.getCommenter().getId().equals(member.getUser().getId())) {
    eventPublisher.publishEvent(new CommentLikeEvent(
        comment.getId(),
        comment.getCommenter().getId(),
        member.getId(),
        member.getNickname()
    ));
}
```

**변경 후**:
```java
User commenter = comment.getCommenter();
if (isNowLiked && commenter != null && !commenter.getId().equals(member.getUser().getId())) {
    eventPublisher.publishEvent(new CommentLikeEvent(
        comment.getId(),
        commenter.getId(),
        member.getId(),
        member.getNickname()
    ));
}
```

**추가 import**:
```java
import moment.user.domain.User;
```

**JPA Lazy Proxy 고려사항**:
- `comment.getCommenter()`는 `@ManyToOne(fetch = FetchType.LAZY)` → lazy proxy 반환
- soft-deleted User의 경우 `@SQLRestriction`에 의해 proxy 초기화 시 `EntityNotFoundException` 가능
- LEFT JOIN이 적용된 쿼리로 Comment를 로드했다면 commenter는 null
- 하지만 Comment의 commenter FK는 `nullable = false`이므로 실제로 proxy가 생성될 수 있음
- **안전하게 null 체크 추가**하되, 실제 동작은 Comment를 로드하는 쿼리에 따라 다름

#### 테스트

**파일 수정/생성**: `src/test/java/moment/like/service/MomentLikeServiceTest.java`, `CommentLikeServiceTest.java`

```
MomentLikeServiceTest:

테스트 1: momenter가_null인_모멘트에_좋아요_시_이벤트가_발행되지_않는다
  - Given: moment (momenter = null), member, likeRepository returns empty
  - When: toggle(moment, member)
  - Then:
    - verify(eventPublisher, never()).publishEvent(any())
    - isNowLiked == true

테스트 2: momenter가_존재하고_본인이_아닌_경우_이벤트가_발행된다
  - Given: moment (momenter = otherUser), member (user != otherUser)
  - When: toggle(moment, member)
  - Then: verify(eventPublisher).publishEvent(any(MomentLikeEvent.class))

CommentLikeServiceTest:

테스트 1: commenter가_null인_댓글에_좋아요_시_이벤트가_발행되지_않는다
  - Given: comment (commenter = null), member
  - When: toggle(comment, member)
  - Then: verify(eventPublisher, never()).publishEvent(any())

테스트 2: commenter가_존재하고_본인이_아닌_경우_이벤트가_발행된다
  - Given: comment (commenter = otherUser), member (user != otherUser)
  - When: toggle(comment, member)
  - Then: verify(eventPublisher).publishEvent(any(CommentLikeEvent.class))
```

---

## 구현 순서 요약

| 순서 | 작업 | 타입 | TDD 사이클 |
|------|------|------|-----------|
| 1 | B-1: MomentRepository LEFT JOIN 변경 (7개 쿼리) | 구조적 변경 | 기존 테스트 통과 확인 |
| 2 | B-2: MomentComposition 테스트 작성 | Red | 테스트 2개 실패 |
| 3 | B-2: MomentComposition null 안전 처리 | Green | 테스트 2개 통과 |
| 4 | B-3: CommentComposition 테스트 작성 | Red | 테스트 2개 실패 |
| 5 | B-3: CommentComposition null 안전 처리 | Green | 테스트 2개 통과 |
| 6 | B-4: CommentApplicationService mapCommentersByComments 변경 | 구조적 변경 | HashMap 변경 |
| 7 | B-5: CommentableMomentResponse 테스트 작성 | Red | 테스트 2개 실패 |
| 8 | B-5: CommentableMomentResponse null 안전 처리 | Green | 테스트 2개 통과 |
| 9 | B-6a: MomentLikeService 테스트 작성 | Red | 테스트 2개 실패 |
| 10 | B-6a: MomentLikeService null 안전 처리 | Green | 테스트 2개 통과 |
| 11 | B-6b: CommentLikeService 테스트 작성 | Red | 테스트 2개 실패 |
| 12 | B-6b: CommentLikeService null 안전 처리 | Green | 테스트 2개 통과 |

---

## Moment/Comment에서 momenter/commenter null이 되는 시나리오 정리

| 시나리오 | momenter | commenter | 처리 |
|----------|----------|-----------|------|
| 정상 유저의 모멘트 | User 객체 | - | 닉네임 표시 |
| 탈퇴 유저의 모멘트 (LEFT JOIN) | null | - | "탈퇴한 사용자" |
| 정상 유저의 댓글 | - | User 객체 | 닉네임 표시 |
| 탈퇴 유저의 댓글 (userById.get → null) | - | null | "탈퇴한 사용자" |
| 탈퇴 유저 모멘트에 좋아요 | null | - | 이벤트 skip |
| 탈퇴 유저 댓글에 좋아요 | - | null 가능 | 이벤트 skip |

---

## GroupCommentResponse 검토

**파일**: `src/main/java/moment/comment/dto/response/GroupCommentResponse.java`

**현재 코드** (line 37):
```java
comment.getMember() != null ? comment.getMember().getNickname() : null,
```

`GroupCommentResponse`는 이미 `comment.getMember()` null 체크를 하고 있음.
이 DTO는 `GroupMember`의 닉네임을 사용하므로, User의 탈퇴와 무관하게 GroupMember의 닉네임을 표시.
**추가 변경 불필요**.

---

## 검증 명령어

```bash
cd /Users/kwonkeonhyeong/Desktop/2025-moment-feat-1059/server

# 구조적 변경 후 기존 테스트 확인
./gradlew fastTest

# 특정 테스트 클래스 실행
./gradlew test --tests "moment.moment.dto.response.tobe.MomentCompositionTest"
./gradlew test --tests "moment.comment.dto.tobe.CommentCompositionTest"
./gradlew test --tests "moment.moment.dto.response.CommentableMomentResponseTest"
./gradlew test --tests "moment.like.service.MomentLikeServiceTest"
./gradlew test --tests "moment.like.service.CommentLikeServiceTest"

# 전체 테스트 (E2E 포함)
./gradlew test

# 전체 빌드
./gradlew build
```

---

## 수정/생성 파일 목록

### 수정 (7개)
| 파일 | 변경 |
|------|------|
| `src/main/java/moment/moment/infrastructure/MomentRepository.java` | 7개 쿼리 `JOIN FETCH` → `LEFT JOIN FETCH` |
| `src/main/java/moment/moment/dto/response/tobe/MomentComposition.java` | `of()` null 안전 처리 |
| `src/main/java/moment/comment/dto/tobe/CommentComposition.java` | `of()` null 안전 처리 |
| `src/main/java/moment/comment/service/application/CommentApplicationService.java` | `mapCommentersByComments` HashMap 사용 |
| `src/main/java/moment/moment/dto/response/CommentableMomentResponse.java` | `of()` null 안전 처리 |
| `src/main/java/moment/like/service/MomentLikeService.java` | momenter null 체크 |
| `src/main/java/moment/like/service/CommentLikeService.java` | commenter null 체크 |

### 신규 생성 - 테스트 (3-5개)
| 파일 | 목적 |
|------|------|
| `src/test/java/moment/moment/dto/response/tobe/MomentCompositionTest.java` | MomentComposition null 처리 테스트 |
| `src/test/java/moment/comment/dto/tobe/CommentCompositionTest.java` | CommentComposition null 처리 테스트 |
| `src/test/java/moment/moment/dto/response/CommentableMomentResponseTest.java` | CommentableMomentResponse null 처리 테스트 |
| `src/test/java/moment/like/service/MomentLikeServiceTest.java` (수정 또는 생성) | momenter null 시 이벤트 미발행 테스트 |
| `src/test/java/moment/like/service/CommentLikeServiceTest.java` (수정 또는 생성) | commenter null 시 이벤트 미발행 테스트 |