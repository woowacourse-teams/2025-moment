# Phase 5: 알림 & 마무리 (Day 10-11)

## 개요
- **목표**: 그룹 기반 알림 시스템 구축, 에러 코드 추가, 최종 테스트
- **원칙**: TDD 기반 (이벤트 → 핸들러 → 알림 생성)
- **검증**: 각 Step 후 `./gradlew test` 및 E2E 시나리오 테스트

---

## Step 1: NotificationType 확장

### 1.1 대상 파일

#### `notification/domain/NotificationType.java` 수정
```java
package moment.notification.domain;

public enum NotificationType {
    // 기존 타입
    NEW_COMMENT,           // 새 코멘트 (기존 유지)

    // 그룹 관련 신규 타입
    GROUP_JOIN_REQUEST,    // 가입 신청 (owner에게)
    GROUP_JOIN_APPROVED,   // 가입 승인됨 (신청자에게)
    GROUP_KICKED,          // 강퇴됨 (강퇴된 멤버에게)

    // 좋아요 관련 신규 타입
    MOMENT_LIKED,          // 모멘트 좋아요 (작성자에게)
    COMMENT_LIKED          // 코멘트 좋아요 (작성자에게)
}
```

### 1.2 TDD 테스트 케이스
```java
@Test
void NotificationType_그룹_관련_타입_존재() {
    assertThat(NotificationType.values()).contains(
        NotificationType.GROUP_JOIN_REQUEST,
        NotificationType.GROUP_JOIN_APPROVED,
        NotificationType.GROUP_KICKED,
        NotificationType.MOMENT_LIKED,
        NotificationType.COMMENT_LIKED
    );
}
```

### 1.3 검증
```bash
./gradlew compileJava
./gradlew fastTest
```

---

## Step 2: TargetType 확장

### 2.1 대상 파일

#### `notification/domain/TargetType.java` 수정
```java
package moment.notification.domain;

public enum TargetType {
    // 기존 타입
    MOMENT,
    COMMENT,

    // 그룹 관련 신규 타입
    GROUP,
    GROUP_MEMBER
}
```

### 2.2 검증
```bash
./gradlew compileJava
```

---

## Step 3: 이벤트 클래스 생성

### 3.1 그룹 관련 이벤트

#### `src/main/java/moment/group/dto/event/GroupJoinRequestEvent.java`
```java
package moment.group.dto.event;

public record GroupJoinRequestEvent(
    Long groupId,
    Long ownerId,          // 알림 수신자
    Long memberId,         // 가입 신청자 멤버 ID
    String applicantNickname
) {}
```

#### `src/main/java/moment/group/dto/event/GroupJoinApprovedEvent.java`
```java
package moment.group.dto.event;

public record GroupJoinApprovedEvent(
    Long groupId,
    Long userId,           // 알림 수신자 (승인된 사용자)
    Long memberId
) {}
```

#### `src/main/java/moment/group/dto/event/GroupKickedEvent.java`
```java
package moment.group.dto.event;

public record GroupKickedEvent(
    Long groupId,
    Long userId,           // 알림 수신자 (강퇴된 사용자)
    Long memberId
) {}
```

### 3.2 좋아요 관련 이벤트

#### `src/main/java/moment/like/dto/event/MomentLikeEvent.java`
```java
package moment.like.dto.event;

public record MomentLikeEvent(
    Long momentId,
    Long momentOwnerId,    // 알림 수신자
    Long likerMemberId,
    String likerNickname
) {}
```

#### `src/main/java/moment/like/dto/event/CommentLikeEvent.java`
```java
package moment.like.dto.event;

public record CommentLikeEvent(
    Long commentId,
    Long commentOwnerId,   // 알림 수신자
    Long likerMemberId,
    String likerNickname
) {}
```

### 3.3 코멘트 이벤트 수정

#### `src/main/java/moment/comment/dto/event/GroupCommentCreateEvent.java`
```java
package moment.comment.dto.event;

public record GroupCommentCreateEvent(
    Long groupId,
    Long momentId,
    Long momentOwnerId,    // 알림 수신자
    Long commentId,
    Long commenterId,
    String commenterNickname
) {}
```

### 3.4 검증
```bash
./gradlew compileJava
```

---

## Step 4: NotificationEventHandler 수정

### 4.1 대상 파일

#### `notification/service/eventHandler/NotificationEventHandler.java` 수정
```java
package moment.notification.service.eventHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.comment.dto.event.GroupCommentCreateEvent;
import moment.group.dto.event.GroupJoinApprovedEvent;
import moment.group.dto.event.GroupJoinRequestEvent;
import moment.group.dto.event.GroupKickedEvent;
import moment.like.dto.event.CommentLikeEvent;
import moment.like.dto.event.MomentLikeEvent;
import moment.notification.domain.NotificationType;
import moment.notification.domain.TargetType;
import moment.notification.service.facade.NotificationFacadeService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationFacadeService notificationFacadeService;

    // 기존 EchoCreateEvent 핸들러 삭제됨 (Phase 1에서 제거)

    /**
     * 그룹 가입 신청 알림 (Owner에게)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupJoinRequestEvent(GroupJoinRequestEvent event) {
        log.info("GroupJoinRequestEvent received: groupId={}, applicant={}",
            event.groupId(), event.applicantNickname());

        notificationFacadeService.createNotificationAndSendSseAndSendToDeviceEndpoint(
            event.ownerId(),              // 수신자: 그룹 소유자
            NotificationType.GROUP_JOIN_REQUEST,
            TargetType.GROUP,
            event.groupId(),              // targetId: 그룹
            event.groupId(),              // groupId
            event.applicantNickname() + "님이 가입을 신청했습니다"
        );
    }

    /**
     * 그룹 가입 승인 알림 (승인된 멤버에게)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupJoinApprovedEvent(GroupJoinApprovedEvent event) {
        log.info("GroupJoinApprovedEvent received: groupId={}, memberId={}",
            event.groupId(), event.memberId());

        notificationFacadeService.createNotificationAndSendSseAndSendToDeviceEndpoint(
            event.userId(),               // 수신자: 승인된 사용자
            NotificationType.GROUP_JOIN_APPROVED,
            TargetType.GROUP,
            event.groupId(),              // targetId: 그룹
            event.groupId(),              // groupId
            "그룹 가입이 승인되었습니다"
        );
    }

    /**
     * 그룹 강퇴 알림 (강퇴된 멤버에게)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupKickedEvent(GroupKickedEvent event) {
        log.info("GroupKickedEvent received: groupId={}, userId={}",
            event.groupId(), event.userId());

        notificationFacadeService.createNotificationAndSendSseAndSendToDeviceEndpoint(
            event.userId(),               // 수신자: 강퇴된 사용자
            NotificationType.GROUP_KICKED,
            TargetType.GROUP,
            event.groupId(),              // targetId: 그룹
            event.groupId(),              // groupId
            "그룹에서 강퇴되었습니다"
        );
    }

    /**
     * 모멘트 좋아요 알림 (모멘트 작성자에게)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMomentLikeEvent(MomentLikeEvent event) {
        log.info("MomentLikeEvent received: momentId={}, liker={}",
            event.momentId(), event.likerNickname());

        notificationFacadeService.createNotificationAndSendSseAndSendToDeviceEndpoint(
            event.momentOwnerId(),        // 수신자: 모멘트 작성자
            NotificationType.MOMENT_LIKED,
            TargetType.MOMENT,
            event.momentId(),             // targetId: 모멘트
            null,                         // groupId (필요시 추가)
            event.likerNickname() + "님이 모멘트를 좋아합니다"
        );
    }

    /**
     * 코멘트 좋아요 알림 (코멘트 작성자에게)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentLikeEvent(CommentLikeEvent event) {
        log.info("CommentLikeEvent received: commentId={}, liker={}",
            event.commentId(), event.likerNickname());

        notificationFacadeService.createNotificationAndSendSseAndSendToDeviceEndpoint(
            event.commentOwnerId(),       // 수신자: 코멘트 작성자
            NotificationType.COMMENT_LIKED,
            TargetType.COMMENT,
            event.commentId(),            // targetId: 코멘트
            null,                         // groupId (필요시 추가)
            event.likerNickname() + "님이 코멘트를 좋아합니다"
        );
    }

    /**
     * 새 코멘트 알림 (그룹 컨텍스트) - 모멘트 작성자에게
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupCommentCreateEvent(GroupCommentCreateEvent event) {
        log.info("GroupCommentCreateEvent received: momentId={}, commenter={}",
            event.momentId(), event.commenterNickname());

        // 자기 글에 자기가 댓글 단 경우 알림 미발송
        if (event.momentOwnerId().equals(event.commenterId())) {
            return;
        }

        notificationFacadeService.createNotificationAndSendSseAndSendToDeviceEndpoint(
            event.momentOwnerId(),        // 수신자: 모멘트 작성자
            NotificationType.NEW_COMMENT,
            TargetType.MOMENT,
            event.momentId(),             // targetId: 모멘트
            event.groupId(),              // groupId
            event.commenterNickname() + "님이 코멘트를 남겼습니다"
        );
    }
}
```

### 4.2 TDD 테스트 케이스
```java
@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

    @Mock
    private NotificationFacadeService notificationFacadeService;

    @InjectMocks
    private NotificationEventHandler eventHandler;

    @Test
    void 그룹_가입_신청_알림_발송() {
        // Given
        GroupJoinRequestEvent event = new GroupJoinRequestEvent(1L, 2L, 3L, "신청자닉네임");

        // When
        eventHandler.handleGroupJoinRequestEvent(event);

        // Then
        verify(notificationFacadeService).createNotificationAndSendSseAndSendToDeviceEndpoint(
            eq(2L),  // ownerId
            eq(NotificationType.GROUP_JOIN_REQUEST),
            eq(TargetType.GROUP),
            eq(1L),  // groupId
            eq(1L),  // groupId
            contains("신청자닉네임")
        );
    }

    @Test
    void 그룹_가입_승인_알림_발송() {
        // Given
        GroupJoinApprovedEvent event = new GroupJoinApprovedEvent(1L, 3L, 4L);

        // When
        eventHandler.handleGroupJoinApprovedEvent(event);

        // Then
        verify(notificationFacadeService).createNotificationAndSendSseAndSendToDeviceEndpoint(
            eq(3L),  // userId
            eq(NotificationType.GROUP_JOIN_APPROVED),
            any(), any(), any(), any()
        );
    }

    @Test
    void 모멘트_좋아요_알림_발송() {
        // Given
        MomentLikeEvent event = new MomentLikeEvent(1L, 2L, 3L, "좋아요닉네임");

        // When
        eventHandler.handleMomentLikeEvent(event);

        // Then
        verify(notificationFacadeService).createNotificationAndSendSseAndSendToDeviceEndpoint(
            eq(2L),  // momentOwnerId
            eq(NotificationType.MOMENT_LIKED),
            eq(TargetType.MOMENT),
            eq(1L),  // momentId
            any(),
            contains("좋아요닉네임")
        );
    }

    @Test
    void 자기_글_코멘트_시_알림_미발송() {
        // Given
        GroupCommentCreateEvent event = new GroupCommentCreateEvent(
            1L, 2L, 3L, 4L, 3L, "닉네임"  // momentOwnerId == commenterId
        );

        // When
        eventHandler.handleGroupCommentCreateEvent(event);

        // Then
        verify(notificationFacadeService, never()).createNotificationAndSendSseAndSendToDeviceEndpoint(
            any(), any(), any(), any(), any(), any()
        );
    }
}
```

### 4.3 검증
```bash
./gradlew fastTest
```

---

## Step 5: NotificationFacadeService 수정

### 5.1 대상 파일

#### `notification/service/facade/NotificationFacadeService.java` 수정
```java
// 기존 메서드 시그니처 변경: groupId 파라미터 추가

public void createNotificationAndSendSseAndSendToDeviceEndpoint(
    Long receiverId,
    NotificationType type,
    TargetType targetType,
    Long targetId,
    Long groupId,        // 신규 파라미터
    String message
) {
    // 1. DB 알림 생성
    Notification notification = notificationApplicationService.createNotification(
        receiverId, type, targetType, targetId, groupId, message
    );

    // 2. SSE 전송
    sseService.sendNotification(receiverId, notification);

    // 3. Firebase Push 전송
    pushNotificationApplicationService.sendPushNotification(receiverId, notification);
}
```

### 5.2 NotificationApplicationService 수정
```java
// 알림 생성 메서드에 groupId 추가

@Transactional
public Notification createNotification(
    Long receiverId,
    NotificationType type,
    TargetType targetType,
    Long targetId,
    Long groupId,
    String message
) {
    Notification notification = new Notification(
        receiverId, type, targetType, targetId, groupId, message
    );
    return notificationRepository.save(notification);
}
```

### 5.3 Notification 엔티티 수정
```java
// 생성자에 groupId 추가

public Notification(
    Long receiverId,
    NotificationType type,
    TargetType targetType,
    Long targetId,
    Long groupId,
    String message
) {
    this.receiverId = receiverId;
    this.type = type;
    this.targetType = targetType;
    this.targetId = targetId;
    this.groupId = groupId;
    this.message = message;
    this.isRead = false;
}
```

### 5.4 검증
```bash
./gradlew compileJava
./gradlew fastTest
```

---

## Step 6: ErrorCode 추가

### 6.1 대상 파일

#### `global/exception/ErrorCode.java` 수정
```java
// 그룹 관련 에러 코드 추가

// 그룹 (GR)
GROUP_NOT_FOUND("GR-001", "그룹을 찾을 수 없습니다"),
GROUP_NAME_REQUIRED("GR-002", "그룹 이름은 필수입니다"),
NOT_GROUP_OWNER("GR-003", "그룹 소유자만 수행할 수 있습니다"),
CANNOT_DELETE_GROUP_WITH_MEMBERS("GR-004", "멤버가 있는 그룹은 삭제할 수 없습니다"),

// 멤버 (MB)
MEMBER_NOT_FOUND("MB-001", "멤버를 찾을 수 없습니다"),
ALREADY_GROUP_MEMBER("MB-002", "이미 그룹 멤버입니다"),
NOT_GROUP_MEMBER("MB-003", "그룹 멤버가 아닙니다"),
CANNOT_KICK_OWNER("MB-004", "소유자는 강퇴할 수 없습니다"),
NICKNAME_ALREADY_USED("MB-005", "이미 사용 중인 닉네임입니다"),
NICKNAME_REQUIRED("MB-006", "닉네임은 필수입니다"),
MEMBER_NOT_PENDING("MB-007", "대기 상태의 멤버가 아닙니다"),
MEMBER_NOT_APPROVED("MB-008", "승인된 멤버가 아닙니다"),
OWNER_CANNOT_LEAVE("MB-009", "소유자는 그룹을 떠날 수 없습니다. 소유권을 이전하세요"),

// 초대 (IN)
INVITE_LINK_EXPIRED("IN-001", "만료된 초대 링크입니다"),
INVITE_LINK_INVALID("IN-002", "유효하지 않은 초대 링크입니다"),
```

### 6.2 검증
```bash
./gradlew compileJava
```

---

## Step 7: NotificationResponse DTO 수정

### 7.1 대상 파일

#### `notification/dto/response/NotificationResponse.java` 수정
```java
package moment.notification.dto.response;

import moment.notification.domain.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String type,
    String targetType,
    Long targetId,
    Long groupId,          // 신규 필드
    String message,
    boolean isRead,
    LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getType().name(),
            notification.getTargetType().name(),
            notification.getTargetId(),
            notification.getGroupId(),
            notification.getMessage(),
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}
```

### 7.2 검증
```bash
./gradlew compileJava
```

---

## Step 8: E2E 시나리오 테스트

### 8.1 전체 플로우 테스트

#### `src/test/java/moment/e2e/GroupFlowE2ETest.java`
```java
@Tag("e2e")
class GroupFlowE2ETest extends AcceptanceTest {

    /**
     * E2E 시나리오:
     * 1. 사용자 A 로그인 → 그룹 생성 → 초대 링크 확인
     * 2. 사용자 B 로그인 → 초대 코드로 가입 신청
     * 3. 사용자 A → 가입 승인
     * 4. 사용자 B → 모멘트 작성 → 이미지 업로드
     * 5. 사용자 A → 코멘트 작성 → 좋아요
     * 6. 알림 확인
     */
    @Test
    void 전체_그룹_플로우_테스트() {
        // 1. 사용자 A 로그인 → 그룹 생성
        String userAToken = 로그인("userA@example.com");
        GroupCreateRequest createRequest = new GroupCreateRequest("테스트 그룹", "설명", "UserA");

        ExtractableResponse<Response> createResponse = given()
            .header("Authorization", "Bearer " + userAToken)
            .contentType(ContentType.JSON)
            .body(createRequest)
            .post("/api/v2/groups");

        assertThat(createResponse.statusCode()).isEqualTo(201);
        Long groupId = createResponse.jsonPath().getLong("groupId");
        String inviteCode = createResponse.jsonPath().getString("inviteCode");

        // 2. 사용자 B 로그인 → 가입 신청
        String userBToken = 로그인("userB@example.com");
        GroupJoinRequest joinRequest = new GroupJoinRequest(inviteCode, "UserB");

        ExtractableResponse<Response> joinResponse = given()
            .header("Authorization", "Bearer " + userBToken)
            .contentType(ContentType.JSON)
            .body(joinRequest)
            .post("/api/v2/groups/join");

        assertThat(joinResponse.statusCode()).isEqualTo(201);
        assertThat(joinResponse.jsonPath().getString("status")).isEqualTo("PENDING");
        Long userBMemberId = joinResponse.jsonPath().getLong("memberId");

        // 3. 사용자 A → 가입 승인
        ExtractableResponse<Response> approveResponse = given()
            .header("Authorization", "Bearer " + userAToken)
            .post("/api/v2/groups/" + groupId + "/members/" + userBMemberId + "/approve");

        assertThat(approveResponse.statusCode()).isEqualTo(200);

        // 4. 사용자 B → 모멘트 작성
        GroupMomentCreateRequest momentRequest = new GroupMomentCreateRequest(
            "오늘의 순간입니다!", List.of()
        );

        ExtractableResponse<Response> momentResponse = given()
            .header("Authorization", "Bearer " + userBToken)
            .contentType(ContentType.JSON)
            .body(momentRequest)
            .post("/api/v2/groups/" + groupId + "/moments");

        assertThat(momentResponse.statusCode()).isEqualTo(201);
        Long momentId = momentResponse.jsonPath().getLong("id");

        // 5. 사용자 A → 코멘트 작성
        CommentCreateRequest commentRequest = new CommentCreateRequest("좋은 순간이네요!");

        ExtractableResponse<Response> commentResponse = given()
            .header("Authorization", "Bearer " + userAToken)
            .contentType(ContentType.JSON)
            .body(commentRequest)
            .post("/api/v2/groups/" + groupId + "/moments/" + momentId + "/comments");

        assertThat(commentResponse.statusCode()).isEqualTo(201);

        // 5-2. 사용자 A → 모멘트 좋아요
        ExtractableResponse<Response> likeResponse = given()
            .header("Authorization", "Bearer " + userAToken)
            .post("/api/v2/groups/" + groupId + "/moments/" + momentId + "/like");

        assertThat(likeResponse.statusCode()).isEqualTo(200);
        assertThat(likeResponse.jsonPath().getBoolean("isLiked")).isTrue();

        // 6. 사용자 B 알림 확인 (코멘트, 좋아요 알림)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ExtractableResponse<Response> notificationResponse = given()
                .header("Authorization", "Bearer " + userBToken)
                .get("/api/v1/notifications");

            assertThat(notificationResponse.statusCode()).isEqualTo(200);
            List<String> types = notificationResponse.jsonPath().getList("type");
            assertThat(types).contains("NEW_COMMENT", "MOMENT_LIKED");
        });
    }

    @Test
    void 그룹_가입_승인_알림_테스트() {
        // Given
        String ownerToken = 로그인("owner@example.com");
        String memberToken = 로그인("member@example.com");
        Long groupId = 그룹_생성_후_ID_반환(ownerToken, "테스트 그룹");
        Long memberId = 가입_신청_후_멤버ID_반환(memberToken, groupId);

        // When: 승인
        given()
            .header("Authorization", "Bearer " + ownerToken)
            .post("/api/v2/groups/" + groupId + "/members/" + memberId + "/approve");

        // Then: 알림 확인
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ExtractableResponse<Response> response = given()
                .header("Authorization", "Bearer " + memberToken)
                .get("/api/v1/notifications");

            assertThat(response.jsonPath().getList("type"))
                .contains("GROUP_JOIN_APPROVED");
        });
    }

    @Test
    void 그룹_강퇴_알림_테스트() {
        // Given
        String ownerToken = 로그인("owner@example.com");
        String memberToken = 로그인("member@example.com");
        Long groupId = 그룹_생성_후_ID_반환(ownerToken, "테스트 그룹");
        Long memberId = 멤버_가입_및_승인(groupId, memberToken, ownerToken);

        // When: 강퇴
        given()
            .header("Authorization", "Bearer " + ownerToken)
            .delete("/api/v2/groups/" + groupId + "/members/" + memberId);

        // Then: 알림 확인
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ExtractableResponse<Response> response = given()
                .header("Authorization", "Bearer " + memberToken)
                .get("/api/v1/notifications");

            assertThat(response.jsonPath().getList("type"))
                .contains("GROUP_KICKED");
        });
    }
}
```

### 8.2 검증
```bash
./gradlew e2eTest --tests "GroupFlowE2ETest"
```

---

## Step 9: 테스트 시드 데이터 업데이트

### 9.1 대상 파일

#### `src/main/resources/sql/test-users.sql` 수정
```sql
-- 기존 star/level 관련 컬럼 참조 제거
-- 그룹/멤버 시드 데이터 추가 (E2E 테스트용)

-- 테스트 그룹
INSERT INTO groups (id, name, description, owner_id, created_at)
VALUES (1, '테스트 그룹', '테스트용 그룹입니다', 1, NOW());

-- 테스트 그룹 멤버 (소유자)
INSERT INTO group_members (id, group_id, user_id, nickname, role, status, created_at)
VALUES (1, 1, 1, 'TestOwner', 'OWNER', 'APPROVED', NOW());

-- 테스트 그룹 멤버 (일반)
INSERT INTO group_members (id, group_id, user_id, nickname, role, status, created_at)
VALUES (2, 1, 2, 'TestMember', 'MEMBER', 'APPROVED', NOW());

-- 테스트 초대 링크
INSERT INTO group_invite_links (id, group_id, code, expired_at, is_active, created_at)
VALUES (1, 1, 'test-invite-code', DATE_ADD(NOW(), INTERVAL 7 DAY), TRUE, NOW());
```

### 9.2 검증
```bash
./gradlew test
```

---

## 최종 검증

### 전체 테스트 실행
```bash
# 1. 컴파일 확인
./gradlew compileJava

# 2. 단위 테스트 (e2e 제외)
./gradlew fastTest

# 3. 전체 테스트 (마이그레이션 포함)
./gradlew test

# 4. E2E 테스트
./gradlew e2eTest

# 5. 전체 빌드
./gradlew build
```

### E2E 시나리오 검증
1. 사용자 A 로그인 → 그룹 생성 → 초대 링크 확인 ✅
2. 사용자 B 로그인 → 초대 코드로 가입 신청 ✅
3. 사용자 A → 가입 승인 ✅
4. 사용자 B → 모멘트 작성 → 이미지 업로드 ✅
5. 사용자 A → 코멘트 작성 → 좋아요 ✅
6. 알림 확인 (가입 승인, 코멘트, 좋아요) ✅

---

## 체크리스트

### NotificationType 확장 완료
- [ ] GROUP_JOIN_REQUEST
- [ ] GROUP_JOIN_APPROVED
- [ ] GROUP_KICKED
- [ ] MOMENT_LIKED
- [ ] COMMENT_LIKED

### TargetType 확장 완료
- [ ] GROUP
- [ ] GROUP_MEMBER

### 이벤트 클래스 생성 완료
- [ ] GroupJoinRequestEvent
- [ ] GroupJoinApprovedEvent
- [ ] GroupKickedEvent
- [ ] MomentLikeEvent
- [ ] CommentLikeEvent
- [ ] GroupCommentCreateEvent

### Event Handler 수정 완료
- [ ] handleGroupJoinRequestEvent
- [ ] handleGroupJoinApprovedEvent
- [ ] handleGroupKickedEvent
- [ ] handleMomentLikeEvent
- [ ] handleCommentLikeEvent
- [ ] handleGroupCommentCreateEvent
- [ ] (삭제) handleEchoCreateEvent

### ErrorCode 추가 완료
- [ ] GR-001 ~ GR-004 (그룹)
- [ ] MB-001 ~ MB-009 (멤버)
- [ ] IN-001 ~ IN-002 (초대)

### 테스트 완료
- [ ] NotificationEventHandler 단위 테스트
- [ ] 전체 그룹 플로우 E2E 테스트
- [ ] 알림 발송 E2E 테스트

### 최종 검증
- [ ] `./gradlew compileJava` 성공
- [ ] `./gradlew fastTest` 성공
- [ ] `./gradlew test` 성공
- [ ] `./gradlew e2eTest` 성공
- [ ] `./gradlew build` 성공

---

## 디렉토리 구조 (최종)

```
src/main/java/moment/
├── auth/
├── comment/
│   ├── domain/
│   │   └── Comment.java          (수정됨: member 필드 추가)
│   ├── dto/
│   │   └── event/
│   │       └── GroupCommentCreateEvent.java
│   └── service/
├── global/
│   └── exception/
│       └── ErrorCode.java        (수정됨: 그룹 관련 에러 추가)
├── group/
│   ├── domain/
│   │   ├── Group.java
│   │   ├── GroupMember.java
│   │   ├── GroupInviteLink.java
│   │   ├── MemberRole.java
│   │   └── MemberStatus.java
│   ├── infrastructure/
│   │   ├── GroupRepository.java
│   │   ├── GroupMemberRepository.java
│   │   └── GroupInviteLinkRepository.java
│   ├── service/
│   │   ├── group/
│   │   │   ├── GroupService.java
│   │   │   └── GroupMemberService.java
│   │   ├── invite/
│   │   │   └── InviteLinkService.java
│   │   └── application/
│   │       ├── GroupApplicationService.java
│   │       └── GroupMemberApplicationService.java
│   ├── presentation/
│   │   ├── GroupController.java
│   │   ├── GroupMemberController.java
│   │   ├── GroupInviteController.java
│   │   ├── GroupMomentController.java
│   │   └── GroupCommentController.java
│   └── dto/
│       ├── request/
│       ├── response/
│       └── event/
│           ├── GroupJoinRequestEvent.java
│           ├── GroupJoinApprovedEvent.java
│           └── GroupKickedEvent.java
├── like/
│   ├── domain/
│   │   ├── MomentLike.java
│   │   └── CommentLike.java
│   ├── infrastructure/
│   │   ├── MomentLikeRepository.java
│   │   └── CommentLikeRepository.java
│   ├── service/
│   │   ├── MomentLikeService.java
│   │   └── CommentLikeService.java
│   └── dto/
│       └── event/
│           ├── MomentLikeEvent.java
│           └── CommentLikeEvent.java
├── moment/
│   ├── domain/
│   │   └── Moment.java           (수정됨: group, member 필드 추가)
│   └── service/
├── notification/
│   ├── domain/
│   │   ├── Notification.java     (수정됨: groupId 필드 추가)
│   │   ├── NotificationType.java (수정됨: 그룹 관련 타입 추가)
│   │   └── TargetType.java       (수정됨: GROUP, GROUP_MEMBER 추가)
│   ├── dto/
│   │   └── response/
│   │       └── NotificationResponse.java (수정됨: groupId 추가)
│   └── service/
│       ├── eventHandler/
│       │   └── NotificationEventHandler.java (수정됨: 6개 핸들러 추가)
│       ├── facade/
│       │   └── NotificationFacadeService.java (수정됨: groupId 파라미터)
│       └── application/
│           └── NotificationApplicationService.java (수정됨)
├── report/
├── storage/
└── user/
    └── domain/
        └── User.java             (수정됨: star/level 제거)
```

---

## 마이그레이션 완료 요약

### Phase 1: 레거시 코드 정리
- Echo, Tag, Reward, Policy, WriteType, Level 시스템 제거
- V25: 레거시 데이터 soft delete
- V26: 레거시 컬럼 제거

### Phase 2: 그룹 인프라 구축
- Group, GroupMember, GroupInviteLink 엔티티
- MomentLike, CommentLike 엔티티
- V27-V33: 신규 테이블 및 컬럼

### Phase 3: 서비스 구현
- GroupService, GroupMemberService, InviteLinkService
- MomentLikeService, CommentLikeService
- upsert/restore 패턴 (Soft Delete + UNIQUE)

### Phase 4: API 구현
- GroupController, GroupMemberController
- GroupInviteController
- GroupMomentController, GroupCommentController
- 26개 REST API 엔드포인트

### Phase 5: 알림 & 마무리
- 6개 신규 NotificationType
- 6개 이벤트 핸들러
- 11개 ErrorCode
- E2E 시나리오 테스트
