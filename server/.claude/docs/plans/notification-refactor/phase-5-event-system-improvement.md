# Phase 5: 이벤트 시스템 개선

---

## Step 5.1: Self-Notification 방지 일원화

### 전략: Publisher 측에서 일관되게 방지

**현재 상태:**
- `MomentLikeService`, `CommentLikeService` -> Publisher에서 방지 O
- `CommentCreateFacadeService` -> 방지 없음 X
- `handleGroupCommentCreateEvent()` -> Handler에서 방지 (불일치)

### 변경

**`comment/service/facade/CommentCreateFacadeService.java`**
```java
// Before: 무조건 이벤트 발행
publisher.publishEvent(CommentCreateEvent.of(moment));

// After: self-notification 체크
if (!moment.getMomenter().getId().equals(userId)) {
    publisher.publishEvent(CommentCreateEvent.of(moment));
}
```

**`notification/service/eventHandler/NotificationEventHandler.java`** (line 127-130)
```java
// Before: 핸들러에서 self-notification 체크
if (event.momentOwnerId().equals(event.commenterId())) { return; }

// After: Publisher로 이동했으므로 핸들러에서 제거
// (handleGroupCommentCreateEvent에서 체크 코드 삭제)
```

---

## Step 5.2: GroupCommentCreateEvent 발행 완성

### 생성할 파일

**`comment/service/facade/GroupCommentCreateFacadeService.java`**
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupCommentCreateFacadeService {
    private final CommentApplicationService commentApplicationService;
    private final MomentApplicationService momentApplicationService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public CommentCreateResponse createGroupComment(
            Long groupId, Long momentId, CommentCreateRequest request, Long userId) {
        Moment moment = momentApplicationService.getMomentBy(momentId);
        CommentCreateResponse response = commentApplicationService.createCommentInGroup(
            groupId, momentId, request, userId);

        // Self-notification 방지 (Publisher 측)
        if (!moment.getMomenter().getId().equals(userId)) {
            publisher.publishEvent(new GroupCommentCreateEvent(
                groupId, momentId, moment.getMomenter().getId(),
                response.commentId(), userId, /* commenterNickname */));
        }
        return response;
    }
}
```

**`group/presentation/GroupCommentController.java`** -- 변경:
```java
// Before
private final CommentApplicationService commentApplicationService;
// 직접 호출: commentApplicationService.createCommentInGroup(...)

// After
private final GroupCommentCreateFacadeService groupCommentCreateFacadeService;
// Facade 호출: groupCommentCreateFacadeService.createGroupComment(...)
```

### TDD 테스트 목록 -- `GroupCommentCreateFacadeServiceTest.java`
```
1. 그룹_댓글_생성_시_GroupCommentCreateEvent를_발행한다
2. 자기_모멘트에_댓글_작성_시_이벤트를_발행하지_않는다
3. 댓글_생성_응답을_반환한다
```

---

## Step 5.3: CommentCreateEvent에 commenterId 추가

**`comment/dto/CommentCreateEvent.java`**
```java
// Before
public record CommentCreateEvent(Long momentId, Long momenterId) {
    public static CommentCreateEvent of(Moment moment) {
        return new CommentCreateEvent(moment.getId(), moment.getMomenter().getId());
    }
}

// After: commenterId 추가 (Step 5.1에서 Publisher 측 체크에 활용)
public record CommentCreateEvent(Long momentId, Long momenterId, Long commenterId) {
    public static CommentCreateEvent of(Moment moment, Long commenterId) {
        return new CommentCreateEvent(moment.getId(), moment.getMomenter().getId(), commenterId);
    }
}
```

**`comment/service/facade/CommentCreateFacadeService.java`** -- 호출부 변경:
```java
publisher.publishEvent(CommentCreateEvent.of(moment, userId));
```