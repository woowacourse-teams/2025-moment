# Phase 5: 댓글/좋아요 생성 차단

- **Status**: DRAFT
- **Created**: 2026-02-09
- **Parent Plan**: [user-block-plan.md](../user-block-plan.md)
- **Depends On**: Phase 1, Phase 2

---

## 목표

차단된 사용자 간의 댓글 작성과 좋아요 생성을 서버 측에서 차단한다.

---

## 설계 근거

조회 필터링(Phase 3, 4)만으로는 부족하다. 차단된 사용자가 직접 API를 호출하여 댓글/좋아요를 생성할 수 있기 때문이다. 생성 시점에서 차단 관계를 확인하고 BL-004 에러를 반환해야 한다.

---

## 현재 상태 분석

### CommentCreateFacadeService

**파일**: `src/main/java/moment/comment/service/facade/CommentCreateFacadeService.java`

```java
@Transactional
public CommentCreateResponse createComment(CommentCreateRequest request, Long userId) {
    commentApplicationService.validateCreateComment(request, userId);
    Moment moment = momentApplicationService.getMomentBy(request.momentId());
    CommentCreateResponse createdComment = commentApplicationService.createComment(request, userId);

    if (!moment.getMomenterId().equals(userId)) {
        publisher.publishEvent(CommentCreateEvent.of(moment, userId));
    }
    return createdComment;
}
```

- 차단 확인 없이 댓글 생성 진행

### GroupCommentCreateFacadeService

**파일**: `src/main/java/moment/comment/service/facade/GroupCommentCreateFacadeService.java`

```java
@Transactional
public GroupCommentResponse createGroupComment(
        Long groupId, Long momentId, Long userId,
        String content, String imageUrl, String imageName) {
    Moment moment = momentApplicationService.getMomentBy(momentId);
    GroupCommentResponse response = commentApplicationService.createCommentInGroup(
            groupId, momentId, userId, content, imageUrl, imageName);

    if (!moment.getMomenterId().equals(userId)) {
        publisher.publishEvent(new GroupCommentCreateEvent(...));
    }
    return response;
}
```

- 동일하게 차단 확인 없음

### MomentApplicationService.toggleMomentLike()

**파일**: `src/main/java/moment/moment/service/application/MomentApplicationService.java`

```java
@Transactional
public boolean toggleMomentLike(Long groupId, Long momentId, Long userId) {
    Moment moment = momentService.getMomentBy(momentId);
    GroupMember member = memberService.getByGroupAndUser(groupId, userId);
    return momentLikeService.toggle(moment, member);
}
```

### CommentApplicationService.toggleCommentLike()

**파일**: `src/main/java/moment/comment/service/application/CommentApplicationService.java`

```java
@Transactional
public boolean toggleCommentLike(Long groupId, Long commentId, Long userId) {
    Comment comment = commentService.getCommentBy(commentId);
    GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);
    return commentLikeService.toggle(comment, member);
}
```

---

## TDD 테스트 목록

### 5-1. 댓글 생성 차단 테스트

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `차단된_사용자의_모멘트에_댓글을_작성하면_예외가_발생한다` | BL-004 |
| T2 | `차단되지_않은_사용자의_모멘트에_댓글을_정상_작성한다` | 정상 케이스 |
| T3 | `그룹_댓글_작성_시_차단된_사용자의_모멘트에는_예외가_발생한다` | BL-004 |

### 5-2. 좋아요 생성 차단 테스트

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `차단된_사용자의_모멘트에_좋아요를_누르면_예외가_발생한다` | BL-004 |
| T2 | `차단된_사용자의_댓글에_좋아요를_누르면_예외가_발생한다` | BL-004 |
| T3 | `차단되지_않은_사용자의_모멘트에_좋아요를_정상_토글한다` | 정상 케이스 |

---

## 구현 단계

### Step 1: CommentCreateFacadeService 수정

**파일 수정**: `src/main/java/moment/comment/service/facade/CommentCreateFacadeService.java`

의존성 추가:
```java
private final UserBlockApplicationService userBlockApplicationService;
```

```java
@Transactional
public CommentCreateResponse createComment(CommentCreateRequest request, Long userId) {
    commentApplicationService.validateCreateComment(request, userId);
    Moment moment = momentApplicationService.getMomentBy(request.momentId());

    // 차단 관계 확인: 댓글 작성자 <-> 모멘트 작성자
    if (userBlockApplicationService.isBlocked(userId, moment.getMomenter().getId())) {
        throw new MomentException(ErrorCode.BLOCKED_USER_INTERACTION);
    }

    CommentCreateResponse createdComment = commentApplicationService.createComment(request, userId);

    if (!moment.getMomenterId().equals(userId)) {
        publisher.publishEvent(CommentCreateEvent.of(moment, userId));
    }
    return createdComment;
}
```

### Step 2: GroupCommentCreateFacadeService 수정

**파일 수정**: `src/main/java/moment/comment/service/facade/GroupCommentCreateFacadeService.java`

의존성 추가:
```java
private final UserBlockApplicationService userBlockApplicationService;
```

```java
@Transactional
public GroupCommentResponse createGroupComment(
        Long groupId, Long momentId, Long userId,
        String content, String imageUrl, String imageName) {
    Moment moment = momentApplicationService.getMomentBy(momentId);

    // 차단 관계 확인: 댓글 작성자 <-> 모멘트 작성자
    if (userBlockApplicationService.isBlocked(userId, moment.getMomenter().getId())) {
        throw new MomentException(ErrorCode.BLOCKED_USER_INTERACTION);
    }

    GroupCommentResponse response = commentApplicationService.createCommentInGroup(
            groupId, momentId, userId, content, imageUrl, imageName);

    if (!moment.getMomenterId().equals(userId)) {
        publisher.publishEvent(new GroupCommentCreateEvent(...));
    }
    return response;
}
```

### Step 3: MomentApplicationService.toggleMomentLike 수정

**파일 수정**: `src/main/java/moment/moment/service/application/MomentApplicationService.java`

```java
@Transactional
public boolean toggleMomentLike(Long groupId, Long momentId, Long userId) {
    Moment moment = momentService.getMomentBy(momentId);

    // 차단 관계 확인: 좋아요 누른 사용자 <-> 모멘트 작성자
    if (userBlockApplicationService.isBlocked(userId, moment.getMomenter().getId())) {
        throw new MomentException(ErrorCode.BLOCKED_USER_INTERACTION);
    }

    GroupMember member = memberService.getByGroupAndUser(groupId, userId);
    return momentLikeService.toggle(moment, member);
}
```

> 참고: `userBlockApplicationService`는 Phase 3에서 이미 의존성 추가됨

### Step 4: CommentApplicationService.toggleCommentLike 수정

**파일 수정**: `src/main/java/moment/comment/service/application/CommentApplicationService.java`

```java
@Transactional
public boolean toggleCommentLike(Long groupId, Long commentId, Long userId) {
    Comment comment = commentService.getCommentBy(commentId);

    // 차단 관계 확인: 좋아요 누른 사용자 <-> 댓글 작성자
    if (userBlockApplicationService.isBlocked(userId, comment.getCommenter().getId())) {
        throw new MomentException(ErrorCode.BLOCKED_USER_INTERACTION);
    }

    GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);
    return commentLikeService.toggle(comment, member);
}
```

> 참고: `userBlockApplicationService`는 Phase 4에서 이미 의존성 추가됨

---

## 생성/수정 파일 목록

| 작업 | 파일 경로 | 변경 내용 |
|------|----------|----------|
| 수정 | `src/main/java/moment/comment/service/facade/CommentCreateFacadeService.java` | 차단 확인 추가 |
| 수정 | `src/main/java/moment/comment/service/facade/GroupCommentCreateFacadeService.java` | 차단 확인 추가 |
| 수정 | `src/main/java/moment/moment/service/application/MomentApplicationService.java` | toggleMomentLike 차단 확인 |
| 수정 | `src/main/java/moment/comment/service/application/CommentApplicationService.java` | toggleCommentLike 차단 확인 |

## 의존성

- Phase 2 완료 필수 (`UserBlockApplicationService.isBlocked()`)
- `MomentApplicationService`에 `userBlockApplicationService`는 Phase 3에서 이미 추가됨
- `CommentApplicationService`에 `userBlockApplicationService`는 Phase 4에서 이미 추가됨

## 주의사항

- 차단 확인은 **생성 로직 이전**에 배치하여 불필요한 DB 작업 방지
- `getMomenter().getId()`로 userId를 가져옴 (`Moment.getMomenter()` returns `User`)
- `getCommenter().getId()`로 userId를 가져옴 (`Comment.getCommenter()` returns `User`)
- 좋아요 차단은 Application Service에서 처리 (Like 도메인 서비스에 외부 의존성을 넣지 않음)
