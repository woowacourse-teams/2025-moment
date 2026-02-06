# 딥링크 라우팅 수정 계획서

> Last Updated: 2026-02-06
> Status: 검토 완료, 구현 대기
> Branch: fix/#1072

## 1. 검토 결과 요약

### 1.1 발견한 사실

**Moment 엔티티 (`moment/domain/Moment.java:46-48`)**:
- `group` 필드가 `@ManyToOne(fetch = FetchType.LAZY)` 로 존재
- `moment.getGroup()` 으로 Group 접근 가능 (그룹 컨텍스트에서만 사용되므로 항상 non-null)
- `moment.getGroup().getId()` 로 groupId 획득 가능

**좋아요 이벤트 발행 흐름**:
- `MomentLikeService.toggle(Moment moment, GroupMember member)` - moment 파라미터에서 `moment.getGroup()` 접근 가능
- `CommentLikeService.toggle(Comment comment, GroupMember member)` - comment에는 group 필드 없음, member에서 `member.getGroup().getId()` 접근 가능

**호출 체인**:
- 모멘트 좋아요: `GroupMomentController` → `MomentApplicationService.toggleMomentLike(groupId, momentId, userId)` → `MomentLikeService.toggle(moment, member)`
- 코멘트 좋아요: `GroupCommentController` → `CommentApplicationService.toggleCommentLike(groupId, commentId, userId)` → `CommentLikeService.toggle(comment, member)`

**핵심 발견**: 좋아요 기능은 **그룹 컨텍스트에서만** 호출됨 (GroupMomentController, GroupCommentController). 따라서 groupId는 항상 존재한다.

### 1.2 원본 계획 대비 수정 사항

**계획 수정 1: MomentLikeEvent에서 groupId 획득 방법**
- 원본 계획: groupId를 이벤트에 추가
- 검증 결과: `MomentLikeService.toggle()` 에서 `moment.getGroup()` 접근 가능. 그룹 컨텍스트에서만 좋아요가 가능하므로 group은 항상 non-null.
- 최종: `moment.getGroup().getId()` 로 groupId를 MomentLikeEvent에 포함

**계획 수정 2: CommentLikeEvent에서 groupId 획득 방법**
- 원본 계획: groupId를 이벤트에 추가
- 검증 결과: `CommentLikeService.toggle()` 에서 `member.getGroup().getId()` 접근 가능. Comment 엔티티 자체에는 group 필드 없지만, GroupMember에는 있음.
- 최종: `member.getGroup().getId()` 로 groupId를 CommentLikeEvent에 포함

### 1.3 하위 호환성 검토

- `MomentLikeEvent`, `CommentLikeEvent` 소비자는 `NotificationEventHandler` 단 하나뿐 (FEATURES.md Cross-Domain Dependencies 확인)
- 이벤트 record에 필드 추가 → 컴파일 에러 발생하므로 발행부와 소비부 동시 수정 필요
- 기존 테스트 (`MomentLikeServiceTest`, `CommentLikeServiceTest`, `NotificationEventHandlerTest`) 모두 수정 필요

### 1.4 보안 검토

- 딥링크의 groupId는 네비게이션 용도일 뿐, 프론트엔드에서 해당 그룹 페이지로 이동 시 별도의 API 호출을 통해 권한 검증이 이루어짐
- groupId가 딥링크에 노출되더라도, 실제 데이터 접근은 API 레벨에서 GroupMember 검증을 수행 (`GroupMemberService.getByGroupAndUser()`)
- 따라서 보안 위험 없음

## 2. 최종 확정 수정 사항

### 2.1 DeepLinkGenerator.java 수정

**파일**: `src/main/java/moment/notification/domain/DeepLinkGenerator.java`

```java
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
```

### 2.2 MomentLikeEvent.java 수정

**파일**: `src/main/java/moment/like/dto/event/MomentLikeEvent.java`

```java
public record MomentLikeEvent(
    Long momentId,
    Long momentOwnerId,
    Long likeMemberId,
    String likerNickname,
    Long groupId        // 추가
) {}
```

### 2.3 CommentLikeEvent.java 수정

**파일**: `src/main/java/moment/like/dto/event/CommentLikeEvent.java`

```java
public record CommentLikeEvent(
    Long commentId,
    Long commentOwnerId,
    Long likeMemberId,
    String likerNickname,
    Long groupId        // 추가
) {}
```

### 2.4 MomentLikeService.java 수정 (이벤트 발행부)

**파일**: `src/main/java/moment/like/service/MomentLikeService.java` (line 42-47)

변경 전:
```java
eventPublisher.publishEvent(new MomentLikeEvent(
    moment.getId(),
    moment.getMomenter().getId(),
    member.getId(),
    member.getNickname()
));
```

변경 후:
```java
eventPublisher.publishEvent(new MomentLikeEvent(
    moment.getId(),
    moment.getMomenter().getId(),
    member.getId(),
    member.getNickname(),
    moment.getGroup().getId()
));
```

### 2.5 CommentLikeService.java 수정 (이벤트 발행부)

**파일**: `src/main/java/moment/like/service/CommentLikeService.java` (line 48-53)

변경 전:
```java
eventPublisher.publishEvent(new CommentLikeEvent(
    comment.getId(),
    comment.getCommenter().getId(),
    member.getId(),
    member.getNickname()
));
```

변경 후:
```java
eventPublisher.publishEvent(new CommentLikeEvent(
    comment.getId(),
    comment.getCommenter().getId(),
    member.getId(),
    member.getNickname(),
    member.getGroup().getId()
));
```

### 2.6 NotificationEventHandler.java 수정 (이벤트 소비부)

**파일**: `src/main/java/moment/notification/service/eventHandler/NotificationEventHandler.java`

**handleMomentLikeEvent** (line 84-93): SourceData에 groupId 추가
```java
notificationFacadeService.notify(new NotificationCommand(
    event.momentOwnerId(),
    NotificationType.MOMENT_LIKED,
    SourceData.of(Map.of("momentId", event.momentId(), "groupId", event.groupId())),
    PushNotificationMessage.MOMENT_LIKED));
```

**handleCommentLikeEvent** (line 97-106): SourceData에 groupId 추가
```java
notificationFacadeService.notify(new NotificationCommand(
    event.commentOwnerId(),
    NotificationType.COMMENT_LIKED,
    SourceData.of(Map.of("commentId", event.commentId(), "groupId", event.groupId())),
    PushNotificationMessage.COMMENT_LIKED));
```

## 3. 수정 대상 파일 전체 목록

| # | 파일 | 변경 유형 | 설명 |
|---|------|-----------|------|
| 1 | `notification/domain/DeepLinkGenerator.java` | 행동적 | 모든 딥링크 경로 수정 |
| 2 | `like/dto/event/MomentLikeEvent.java` | 행동적 | groupId 필드 추가 |
| 3 | `like/dto/event/CommentLikeEvent.java` | 행동적 | groupId 필드 추가 |
| 4 | `like/service/MomentLikeService.java` | 행동적 | 이벤트 발행 시 groupId 포함 |
| 5 | `like/service/CommentLikeService.java` | 행동적 | 이벤트 발행 시 groupId 포함 |
| 6 | `notification/service/eventHandler/NotificationEventHandler.java` | 행동적 | SourceData에 groupId 포함 |

### 수정 대상 테스트 파일

| # | 파일 | 설명 |
|---|------|------|
| 1 | `notification/domain/DeepLinkGeneratorTest.java` | 모든 딥링크 기대값 수정 |
| 2 | `like/service/MomentLikeServiceTest.java` | MomentLikeEvent 생성자 호출 수정 |
| 3 | `like/service/CommentLikeServiceTest.java` | CommentLikeEvent 생성자 호출 수정 |
| 4 | `notification/service/eventHandler/NotificationEventHandlerTest.java` | 이벤트 & SourceData 수정 |

## 4. TDD 기반 구현 단계

### Phase 1: DeepLinkGenerator 딥링크 경로 수정

> 개인 모멘트(group == null)는 레거시 기능으로 제외. 모든 알림은 그룹 컨텍스트에서만 발생.

**Step 1-1**: 그룹 모멘트 댓글 딥링크 테스트 수정 (RED)
- `DeepLinkGeneratorTest.그룹_모멘트_댓글_알림의_딥링크를_생성한다()`
- 기대값: `/groups/3/moments/42` → `/groups/3/collection/my-moment`

**Step 1-2**: 구현 (GREEN)
- `NEW_COMMENT_ON_MOMENT` → `/groups/{groupId}/collection/my-moment`

**Step 1-3**: 그룹 가입 신청 딥링크 테스트 수정 (RED)
- 기대값: `/groups/3` → `/groups/3/today-moment`

**Step 1-4**: 구현 (GREEN)

**Step 1-5**: 그룹 가입 승인 딥링크 테스트 수정 (RED)
- 기대값: `/groups/3` → `/groups/3/today-moment`

**Step 1-6**: 구현 (GREEN)

**Step 1-7**: 모멘트 좋아요 딥링크 테스트 수정 (RED)
- SourceData에 groupId 추가 필요
- 기대값: `/moments/42` → `/groups/3/collection/my-moment`

**Step 1-8**: 구현 (GREEN)
- `MOMENT_LIKED` → `/groups/{groupId}/collection/my-moment`

**Step 1-9**: 코멘트 좋아요 딥링크 테스트 수정 (RED)
- SourceData에 groupId 추가 필요
- 기대값: `/comments/15` → `/groups/3/collection/my-comment`

**Step 1-10**: 구현 (GREEN)
- `COMMENT_LIKED` → `/groups/{groupId}/collection/my-comment`

### Phase 2: 좋아요 이벤트에 groupId 추가

**Step 2-1**: MomentLikeEvent에 groupId 필드 추가
- record 수정, 기존 테스트 컴파일 에러 해결
- `MomentLikeServiceTest` 에서 이벤트 생성자 호출 수정

**Step 2-2**: MomentLikeService 이벤트 발행부 수정
- `moment.getGroup().getId()` 추가

**Step 2-3**: CommentLikeEvent에 groupId 필드 추가
- record 수정, 기존 테스트 컴파일 에러 해결
- `CommentLikeServiceTest` 에서 이벤트 생성자 호출 수정

**Step 2-4**: CommentLikeService 이벤트 발행부 수정
- `member.getGroup().getId()` 추가

### Phase 3: NotificationEventHandler SourceData 수정

**Step 3-1**: handleMomentLikeEvent 테스트 수정 (RED)
- `NotificationEventHandlerTest.모멘트_좋아요_이벤트_시_알림을_발송한다()`
- MomentLikeEvent 생성자에 groupId 추가
- 기대 SourceData에 groupId 포함

**Step 3-2**: handleMomentLikeEvent 구현 수정 (GREEN)

**Step 3-3**: handleCommentLikeEvent 테스트 수정 (RED)
- `NotificationEventHandlerTest.코멘트_좋아요_이벤트_시_알림을_발송한다()`
- CommentLikeEvent 생성자에 groupId 추가
- 기대 SourceData에 groupId 포함

**Step 3-4**: handleCommentLikeEvent 구현 수정 (GREEN)

### Phase 4: 전체 테스트 실행 및 검증

**Step 4-1**: `./gradlew fastTest` 로 전체 단위 테스트 통과 확인

## 5. 커밋 전략 (Tidy First)

이 변경은 모두 **행동적 변경**(딥링크 경로 수정)이므로, 구조적 변경과 분리할 필요 없음.

### 커밋 1: 딥링크 경로 수정 + 이벤트 groupId 추가
```
fix: 알림 딥링크를 클라이언트 라우트에 맞게 수정

- DeepLinkGenerator: 모든 경로를 프론트엔드 라우트에 매칭
- MomentLikeEvent, CommentLikeEvent: groupId 필드 추가
- NotificationEventHandler: SourceData에 groupId 포함
- 관련 테스트 전체 수정
```

단일 커밋으로 진행하는 이유:
- 이벤트 record에 필드를 추가하면 기존 코드 컴파일 에러가 발생하므로, 발행부/소비부/딥링크 생성부를 함께 수정해야 빌드가 통과함
- 모든 변경이 동일한 목적(딥링크 경로 불일치 수정)을 위한 것

## 6. 경로 매핑 요약 (최종)

| NotificationType | SourceData | 현재 딥링크 | 수정 후 딥링크 |
|---|---|---|---|
| NEW_COMMENT_ON_MOMENT | `{momentId, groupId}` | `/groups/{groupId}/moments/{momentId}` | `/groups/{groupId}/collection/my-moment` |
| GROUP_JOIN_REQUEST | `{groupId}` | `/groups/{groupId}` | `/groups/{groupId}/today-moment` |
| GROUP_JOIN_APPROVED | `{groupId}` | `/groups/{groupId}` | `/groups/{groupId}/today-moment` |
| GROUP_KICKED | `{groupId}` | `null` | `null` (유지) |
| MOMENT_LIKED | `{momentId}` → `{momentId, groupId}` | `/moments/{momentId}` | `/groups/{groupId}/collection/my-moment` |
| COMMENT_LIKED | `{commentId}` → `{commentId, groupId}` | `/comments/{commentId}` | `/groups/{groupId}/collection/my-comment` |

> 개인 모멘트(group == null)는 레거시 기능으로 수정 대상에서 제외.
