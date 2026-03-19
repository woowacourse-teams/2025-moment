# 응답 DTO에 memberId 필드 추가

> Created: 2026-02-09
> Updated: 2026-02-09
> Status: REVIEWED

## Context

기존 `GroupMomentResponse`, `GroupCommentResponse`에는 `memberId` 필드가 포함되어 있으나,
일부 응답 DTO(`CommentableMomentResponse`, `CommentComposition`, `MyGroupMomentCommentResponse`)에는 누락되어 있다.
API 응답의 일관성을 위해 동일한 null-safe 패턴으로 `memberId` 필드를 추가한다.

## 리뷰 반영 사항

| 리뷰 이슈 | 심각도 | 반영 내용 |
|-----------|--------|----------|
| N+1 Lazy Loading (`CommentComposition.of()`) | CRITICAL | Phase 1에서 Repository 쿼리에 LEFT JOIN FETCH 추가 |
| Lazy Loading (`CommentableMomentResponse.of()`) | HIGH | Phase 1에서 Repository 쿼리에 LEFT JOIN FETCH 추가 |
| 테스트 커버리지 부족 | HIGH | Phase 2, 3에 memberId 매핑 테스트 추가 |
| Feature Registry 업데이트 누락 | MEDIUM | Phase 4에 문서 업데이트 단계 추가 |

## 변경 파일 요약

| 파일 | 변경 내용 | 단계 |
|------|----------|------|
| `MomentRepository.java` | `findAllWithMomenterByIds` 쿼리에 `LEFT JOIN FETCH m.member` 추가 | 1 |
| `CommentRepository.java` | `findAllWithMemberByMomentIdIn` 쿼리 추가 | 1 |
| `CommentService.java` | `of()` 호출 경로에서 새 쿼리 메서드 사용 | 1 |
| `CommentableMomentResponse.java` | `Long memberId` 필드 추가 + `of()` 매핑 | 2 |
| `CommentableMomentFacadeServiceTest.java` | 생성자 인자 수 수정 (5→6) | 2 |
| `CommentComposition.java` | `Long memberId` 필드 추가 + `of()` 매핑 | 3 |
| `CommentCompositionTest.java` | memberId null/non-null 매핑 테스트 추가 | 3 |
| `MyGroupCommentPageFacadeService.java` | 직접 생성자 호출에 8번째 인자 추가 | 3 |
| `MyGroupMomentCommentResponse.java` | `Long memberId` 필드 추가 + `of()` 매핑 | 3 |

---

## Phase 1: Repository 쿼리 수정 (Lazy Loading 방지)

> Tidy First: 구조적 변경 — 동작 변경 없이 쿼리 최적화

### Step 1-1. MomentRepository 쿼리 수정

**파일**: `src/main/java/moment/moment/infrastructure/MomentRepository.java`

`findAllWithMomenterByIds` 쿼리에 `LEFT JOIN FETCH m.member` 추가:

```java
// Before
@Query("""
      SELECT m
      FROM moments m
      LEFT JOIN FETCH m.momenter
      WHERE m.id IN :momentIds
       """)
List<Moment> findAllWithMomenterByIds(@Param("momentIds") List<Long> momentIds);

// After
@Query("""
      SELECT m
      FROM moments m
      LEFT JOIN FETCH m.momenter
      LEFT JOIN FETCH m.member
      WHERE m.id IN :momentIds
       """)
List<Moment> findAllWithMomenterByIds(@Param("momentIds") List<Long> momentIds);
```

**이유**: Phase 2에서 `CommentableMomentResponse.of()` 내 `moment.getMember()` 접근 시 추가 쿼리(+1) 방지.

### Step 1-2. CommentRepository 쿼리 추가

**파일**: `src/main/java/moment/comment/infrastructure/CommentRepository.java`

`findAllByMomentIdIn`은 Spring Data 파생 쿼리로 member를 fetch하지 않음. member를 함께 로드하는 쿼리 추가:

```java
@Query("""
      SELECT c
      FROM comments c
      LEFT JOIN FETCH c.member
      WHERE c.momentId IN :momentIds
       """)
List<Comment> findAllWithMemberByMomentIdIn(@Param("momentIds") List<Long> momentIds);
```

**이유**: Phase 3에서 `CommentComposition.of()` 내 `comment.getMember()` 접근 시 N+1 쿼리 방지.

### Step 1-3. CommentService에서 새 쿼리 사용

`CommentComposition.of()` 호출 경로의 comment 조회를 `findAllWithMemberByMomentIdIn`으로 변경.

> 기존 `findAllByMomentIdIn` 호출 지점 중 `CommentComposition.of()` 경로만 변경. 다른 호출 지점은 영향 없는지 확인 필요.

### Step 1-4. 검증

```bash
./gradlew fastTest
```

기존 테스트가 모두 통과하는지 확인 (동작 변경 없으므로 깨지면 안 됨)

---

## Phase 2: CommentableMomentResponse에 memberId 추가

### Step 2-1. CommentableMomentResponse 수정

**파일**: `src/main/java/moment/moment/dto/response/CommentableMomentResponse.java`

record 필드에 `memberId` 추가 (id 다음 위치):

```java
// Before
public record CommentableMomentResponse(
        Long id,
        String nickname,
        String content,
        String imageUrl,
        LocalDateTime createdAt
)

// After
public record CommentableMomentResponse(
        Long id,
        @Schema(description = "작성자 멤버 ID", example = "1")
        Long memberId,
        String nickname,
        String content,
        String imageUrl,
        LocalDateTime createdAt
)
```

`of()` return문에 매핑 추가:

```java
return new CommentableMomentResponse(
        moment.getId(),
        moment.getMember() != null ? moment.getMember().getId() : null,  // 추가
        nickname,
        moment.getContent(),
        momentImageUrl,
        moment.getCreatedAt());
```

> `moment.getMember()`는 Phase 1에서 LEFT JOIN FETCH로 이미 로드됨. Lazy loading 안전.

### Step 2-2. 테스트 수정

**파일**: `src/test/java/moment/moment/service/facade/CommentableMomentFacadeServiceTest.java`

```java
// Before (87행)
CommentableMomentResponse expected = new CommentableMomentResponse(
        20L, "작성자", "모멘트 내용", null, null);

// After
CommentableMomentResponse expected = new CommentableMomentResponse(
        20L, null, "작성자", "모멘트 내용", null, null);
```

### Step 2-3. 검증

```bash
./gradlew test --tests "moment.moment.service.facade.CommentableMomentFacadeServiceTest"
```

---

## Phase 3: CommentComposition + MyGroupMomentCommentResponse에 memberId 추가

### Step 3-1. CommentComposition 수정

**파일**: `src/main/java/moment/comment/dto/tobe/CommentComposition.java`

record 필드 맨 마지막에 추가:

```java
// Before
public record CommentComposition(
        Long id, String content, String nickname, String imageUrl,
        LocalDateTime commentCreatedAt, Long momentId, Long commenterUserId
)

// After
public record CommentComposition(
        Long id, String content, String nickname, String imageUrl,
        LocalDateTime commentCreatedAt, Long momentId, Long commenterUserId,
        Long memberId
)
```

`of()` return문에 매핑 추가:

```java
return new CommentComposition(
        comment.getId(),
        comment.getContent(),
        commenter != null ? commenter.getNickname() : "탈퇴한 사용자",
        imageUrl,
        comment.getCreatedAt(),
        comment.getMomentId(),
        commenter != null ? commenter.getId() : null,
        comment.getMember() != null ? comment.getMember().getId() : null  // 추가
);
```

> `comment.getMember()`는 Phase 1에서 `findAllWithMemberByMomentIdIn`으로 이미 로드됨. Lazy loading 안전.

### Step 3-2. CommentCompositionTest에 memberId 매핑 테스트 추가

**파일**: `src/test/java/moment/comment/dto/tobe/CommentCompositionTest.java`

```java
@Test
void member가_null인_경우_memberId가_null이다() {
    // given
    Comment comment = createCommentStub();  // member 미설정
    User commenter = UserFixture.createUserWithId(1L);

    // when
    CommentComposition composition = CommentComposition.of(comment, commenter, "imageUrl");

    // then
    assertThat(composition.memberId()).isNull();
}
```

> member가 존재하는 경우의 매핑 테스트는 GroupMember 설정이 필요하므로 통합 테스트에서 검증.

### Step 3-3. MyGroupCommentPageFacadeService 수정 (직접 생성자 호출)

**파일**: `src/main/java/moment/comment/service/facade/MyGroupCommentPageFacadeService.java` (140-148행)

```java
// Before
composition = new CommentComposition(
        comment.getId(),
        comment.getContent(),
        comment.getMember() != null ? comment.getMember().getNickname() : null,
        null,
        comment.getCreatedAt(),
        comment.getMomentId(),
        comment.getCommenter() != null ? comment.getCommenter().getId() : null
);

// After - 8번째 인자 추가
composition = new CommentComposition(
        comment.getId(),
        comment.getContent(),
        comment.getMember() != null ? comment.getMember().getNickname() : null,
        null,
        comment.getCreatedAt(),
        comment.getMomentId(),
        comment.getCommenter() != null ? comment.getCommenter().getId() : null,
        comment.getMember() != null ? comment.getMember().getId() : null
);
```

> 이 코드 경로의 comment는 `findByMemberIdOrderByIdDesc`로 로드되며, 기존 `LEFT JOIN FETCH c.member` 포함. Lazy loading 안전.

### Step 3-4. MyGroupMomentCommentResponse 수정

**파일**: `src/main/java/moment/moment/dto/response/MyGroupMomentCommentResponse.java`

record 필드에 `memberId` 추가 (id 다음 위치):

```java
// Before
public record MyGroupMomentCommentResponse(
        Long id, String content, String memberNickname, String imageUrl,
        LocalDateTime createdAt, long likeCount, boolean hasLiked
)

// After
public record MyGroupMomentCommentResponse(
        Long id,
        @Schema(description = "작성자 멤버 ID", example = "1")
        Long memberId,
        String content, String memberNickname, String imageUrl,
        LocalDateTime createdAt, long likeCount, boolean hasLiked
)
```

`of()` return문에 매핑 추가:

```java
return new MyGroupMomentCommentResponse(
        composition.id(),
        composition.memberId(),  // 추가
        composition.content(),
        composition.nickname(),
        composition.imageUrl(),
        composition.commentCreatedAt(),
        likeCount,
        hasLiked
);
```

### Step 3-5. 검증

```bash
./gradlew fastTest
```

---

## Phase 4: Feature Registry 업데이트

### Step 4-1. 문서 업데이트

- `.claude/docs/features/moment.md` — CommentableMomentResponse 변경 기록
- `.claude/docs/features/comment.md` — CommentComposition, MyGroupMomentCommentResponse 변경 기록
- `.claude/docs/features/FEATURES.md` — Recent Changes에 기록

---

## 변경 여파 분석

### 컴파일 오류 발생 지점 (수정 필수)

| 파일 | 원인 | 해결 |
|------|------|------|
| `CommentableMomentFacadeServiceTest.java:87` | 생성자 인자 5→6 | Step 2-2 |
| `MyGroupCommentPageFacadeService.java:140` | 생성자 인자 7→8 | Step 3-3 |

### 영향 없는 호출 지점 (of() 시그니처 불변)

- `MomentApplicationService.java:157` — `CommentableMomentResponse.of()` 호출
- `CommentApplicationService.java:69,151` — `CommentComposition.of()` 호출
- `MyGroupMomentPageFacadeService.java:138` — `MyGroupMomentCommentResponse.of()` 호출
- `CommentCompositionTest.java:22,35` — `CommentComposition.of()` 호출

### Lazy Loading 안전성 (Phase 1에서 해결)

| 호출 경로 | 쿼리 | member fetch | 상태 |
|-----------|------|-------------|------|
| `MomentApplicationService.pickRandomMomentComposition()` | `findAllWithMomenterByIds` | Phase 1에서 추가 | 안전 |
| `CommentApplicationService.getMyCommentCompositionsBy()` | `findAllWithMemberByMomentIdIn` (신규) | Phase 1에서 추가 | 안전 |
| `MyGroupCommentPageFacadeService` 폴백 경로 | `findByMemberIdOrderByIdDesc` | 기존 포함 | 안전 |
