# Phase 4: 댓글 필터링

- **Status**: DRAFT
- **Created**: 2026-02-09
- **Parent Plan**: [user-block-plan.md](../user-block-plan.md)
- **Depends On**: Phase 1, Phase 2, Phase 3

---

## 목표

차단된 사용자의 댓글을 조회에서 제외하고, 댓글 count를 보정한다.

---

## 설계 결정

**Java 필터링 채택 근거**: 댓글은 단일 모멘트의 전체 댓글을 로드 (페이지네이션 없음)하므로 Java 필터링이 적합. DB 쿼리 필터링으로 변경하면 `CommentService.getAllByMomentIds()`의 모든 호출부에 영향이 가므로 변경 범위가 과도함. 모멘트(Phase 3)의 DB 필터링과 접근 방식이 다르지만 의도적 선택.

---

## 현재 상태 분석

### CommentApplicationService (line 196-211)

**파일**: `src/main/java/moment/comment/service/application/CommentApplicationService.java`

```java
public List<GroupCommentResponse> getCommentsInGroup(Long groupId, Long momentId, Long userId) {
    GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);
    List<Comment> comments = commentService.getAllByMomentIds(List.of(momentId));
    // ... 댓글 목록을 GroupCommentResponse로 변환
}
```

- `getMyCommentCompositionsBy(List<Long> momentIds)` (line 49-72): 모든 댓글을 조회하여 CommentComposition으로 변환. 차단 필터링 없음.

### CommentComposition (현재 상태 - CRITICAL)

**파일**: `src/main/java/moment/comment/dto/tobe/CommentComposition.java`

> **주의**: 실제 코드를 확인하여 `commenterUserId` 필드 존재 여부를 검증해야 함. 없다면 구조적 변경 선행 필요.

**문제**: `commenterUserId` 필드가 없을 경우, Java 필터링을 하려면 댓글 작성자의 userId가 필요하지만 현재 `CommentComposition`에는 이 정보가 없다.

**해결**: `commenterUserId` 필드를 추가하는 구조적 변경이 선행되어야 한다 (Tidy First - 별도 커밋).

### MyGroupMomentPageFacadeService

**파일**: `src/main/java/moment/moment/service/facade/MyGroupMomentPageFacadeService.java`

```java
private MyGroupMomentListResponse buildMomentListResponse(List<Moment> moments, Long memberId, Long userId) {
    List<Long> momentIds = moments.stream().map(Moment::getId).toList();
    List<CommentComposition> allComments = commentApplicationService.getMyCommentCompositionsBy(momentIds);
    // ... 댓글을 모멘트별로 그룹핑
}
```

- `allComments`에서 차단된 사용자의 댓글을 필터링해야 함

### MyGroupCommentPageFacadeService

**파일**: `src/main/java/moment/comment/service/facade/MyGroupCommentPageFacadeService.java`

- `buildCommentListResponse()` 내에서 `commentApplicationService.getMyCommentCompositionsBy(momentIds)` 결과에서 차단된 사용자의 댓글 필터링 필요

### CommentRepository.countByMomentId

**파일**: `src/main/java/moment/comment/infrastructure/CommentRepository.java`

```java
long countByMomentId(Long momentId);
```

- 차단 사용자의 댓글도 포함하여 카운트. "댓글 5개" 표시 vs 실제 3개 노출 UX 불일치.

---

## TDD 테스트 목록

### 4-1. CommentComposition 구조 변경 테스트

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `CommentComposition에_commenterUserId가_포함된다` | 필드 추가 확인 |

### 4-2. CommentApplicationService 테스트

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `그룹_댓글_조회_시_차단된_사용자의_댓글을_제외한다` | Java 필터링 |
| T2 | `차단_목록이_비어있으면_모든_댓글을_반환한다` | 빈 차단 목록 |

### 4-3. 댓글 count 보정 테스트

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `댓글_count가_차단된_사용자_댓글을_제외한_수를_반환한다` | DB 레벨 count |

---

## 구현 단계

### Step 1: CommentComposition에 commenterUserId 추가 (구조적 변경 - 별도 커밋)

**파일 수정**: `src/main/java/moment/comment/dto/tobe/CommentComposition.java`

`commenterUserId` (Long) 필드 추가. `of()` 팩토리 메서드와 직접 생성자 호출부 모두 수정.

**영향 범위**: `CommentComposition`을 직접 생성하는 곳 확인 필요.

`MyGroupCommentPageFacadeService.java`에서 직접 생성하는 코드도 `commenterUserId` 인자 추가 필요.

> **Tidy First**: 이 구조적 변경은 별도 커밋으로 분리한다. 동작 변경 없이 필드만 추가.

### Step 2: CommentApplicationService에 댓글 필터링 추가

**파일 수정**: `src/main/java/moment/comment/service/application/CommentApplicationService.java`

의존성 추가:
```java
private final UserBlockApplicationService userBlockApplicationService;
```

**`getCommentsInGroup()` 수정**:
```java
public List<GroupCommentResponse> getCommentsInGroup(Long groupId, Long momentId, Long userId) {
    GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);
    List<Comment> comments = commentService.getAllByMomentIds(List.of(momentId));

    // 차단된 사용자 필터링
    List<Long> blockedUserIds = userBlockApplicationService.getBlockedUserIds(userId);
    Set<Long> blockedUserIdSet = new HashSet<>(blockedUserIds);
    comments = comments.stream()
            .filter(c -> !blockedUserIdSet.contains(c.getCommenter().getId()))
            .toList();

    // ... 이하 기존 로직 동일
}
```

### Step 3: MyGroupMomentPageFacadeService에 댓글 필터링 추가

**파일 수정**: `src/main/java/moment/moment/service/facade/MyGroupMomentPageFacadeService.java`

의존성 추가:
```java
private final UserBlockApplicationService userBlockApplicationService;
```

**`buildMomentListResponse()` 수정**:
```java
private MyGroupMomentListResponse buildMomentListResponse(List<Moment> moments, Long memberId, Long userId) {
    // 차단된 사용자 ID 조회 (한 번만)
    List<Long> blockedUserIds = userBlockApplicationService.getBlockedUserIds(userId);
    Set<Long> blockedUserIdSet = new HashSet<>(blockedUserIds);

    List<CommentComposition> allComments = commentApplicationService.getMyCommentCompositionsBy(momentIds);

    // 차단된 사용자 댓글 필터링
    allComments = allComments.stream()
            .filter(c -> c.commenterUserId() == null || !blockedUserIdSet.contains(c.commenterUserId()))
            .toList();

    // ... 이하 기존 로직 동일
}
```

### Step 4: MyGroupCommentPageFacadeService에 댓글 필터링 추가

**파일 수정**: `src/main/java/moment/comment/service/facade/MyGroupCommentPageFacadeService.java`

의존성 추가:
```java
private final UserBlockApplicationService userBlockApplicationService;
```

**`buildCommentListResponse()` 수정**:
```java
private MyGroupCommentListResponse buildCommentListResponse(...) {
    // 차단된 사용자 ID 조회
    List<Long> blockedUserIds = userBlockApplicationService.getBlockedUserIds(userId);
    Set<Long> blockedUserIdSet = new HashSet<>(blockedUserIds);

    List<CommentComposition> allCommentCompositions =
            commentApplicationService.getMyCommentCompositionsBy(momentIds);

    // 차단된 사용자 댓글 필터링
    allCommentCompositions = allCommentCompositions.stream()
            .filter(c -> c.commenterUserId() == null || !blockedUserIdSet.contains(c.commenterUserId()))
            .toList();

    // ... 이하 기존 로직 동일
}
```

### Step 5: 댓글 count 보정

**파일 수정**: `src/main/java/moment/comment/infrastructure/CommentRepository.java`

```java
@Query("""
    SELECT COUNT(c) FROM comments c
    WHERE c.momentId = :momentId
      AND c.commenter.id NOT IN :blockedUserIds
    """)
long countByMomentIdExcludingBlocked(
        @Param("momentId") Long momentId,
        @Param("blockedUserIds") List<Long> blockedUserIds);
```

**파일 수정**: `src/main/java/moment/comment/service/comment/CommentService.java`

```java
public long countByMomentIdExcludingBlocked(Long momentId, List<Long> blockedUserIds) {
    if (blockedUserIds == null || blockedUserIds.isEmpty()) {
        return commentRepository.countByMomentId(momentId);
    }
    return commentRepository.countByMomentIdExcludingBlocked(momentId, blockedUserIds);
}
```

**파일 수정**: `src/main/java/moment/moment/service/application/MomentApplicationService.java`

`getGroupMoments()` 내 댓글 count 부분:
```java
// AS-IS
long commentCount = commentService.countByMomentId(moment.getId());

// TO-BE
long commentCount = commentService.countByMomentIdExcludingBlocked(moment.getId(), blockedUserIds);
```

---

## 생성/수정 파일 목록

| 작업 | 파일 경로 | 변경 내용 |
|------|----------|----------|
| 수정 | `src/main/java/moment/comment/dto/tobe/CommentComposition.java` | `commenterUserId` 필드 추가 (구조적 변경, 별도 커밋) |
| 수정 | `src/main/java/moment/comment/service/application/CommentApplicationService.java` | `UserBlockApplicationService` 의존성 + 댓글 필터링 |
| 수정 | `src/main/java/moment/moment/service/facade/MyGroupMomentPageFacadeService.java` | 댓글 필터링 추가 |
| 수정 | `src/main/java/moment/comment/service/facade/MyGroupCommentPageFacadeService.java` | 댓글 필터링 추가 |
| 수정 | `src/main/java/moment/comment/infrastructure/CommentRepository.java` | `countByMomentIdExcludingBlocked` 쿼리 추가 |
| 수정 | `src/main/java/moment/comment/service/comment/CommentService.java` | `countByMomentIdExcludingBlocked` 메서드 추가 |
| 수정 | `src/main/java/moment/moment/service/application/MomentApplicationService.java` | 댓글 count 보정 |

## 의존성

- Phase 2 완료 필수 (`UserBlockApplicationService`)
- `CommentComposition` 구조적 변경은 **별도 커밋으로 선행** (Tidy First)

## 주의사항

- `CommentComposition` 필드 추가 시 직접 생성자 호출하는 곳도 수정 필요 - 컴파일러가 감지함
- `null` 체크: 탈퇴한 사용자의 `commenterUserId`가 null일 수 있으므로 `c.commenterUserId() == null`인 경우 필터링하지 않음
- `Set<Long>` 사용: `List.contains()` O(n) 대신 `HashSet.contains()` O(1) 해시 룩업
