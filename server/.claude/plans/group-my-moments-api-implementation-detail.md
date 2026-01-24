# GET /api/v2/groups/{groupId}/my-moments API 확장 - 세부 구현 계획

> 원본 계획: `group-my-moments-api-enhancement.md`

---

## Phase 1: 새 API 구현 (기능 추가)

### Step 1.1: MyGroupMomentCommentResponse.java 생성

**파일 경로**: `server/src/main/java/moment/moment/dto/response/MyGroupMomentCommentResponse.java`

**참고 파일**:
- `moment/dto/response/MyMomentCommentResponse.java` (기존 패턴)
- `comment/dto/tobe/CommentComposition.java` (변환 소스)

**CommentComposition 현재 필드** (확인됨):
```java
// comment/dto/tobe/CommentComposition.java
public record CommentComposition(
    Long id,              // 댓글 ID
    String content,       // 댓글 내용
    String nickname,      // 작성자 닉네임
    String imageUrl,      // 이미지 URL
    LocalDateTime commentCreatedAt,  // 생성 시간
    Long momentId         // 모멘트 ID
)
```

**구현 내용 (확정 - memberId 제외)**:
```java
package moment.moment.dto.response;

import moment.comment.dto.tobe.CommentComposition;
import java.time.LocalDateTime;

public record MyGroupMomentCommentResponse(
    Long id,
    String content,
    String memberNickname,
    LocalDateTime createdAt
) {
    public static MyGroupMomentCommentResponse from(CommentComposition composition) {
        return new MyGroupMomentCommentResponse(
            composition.id(),
            composition.content(),
            composition.nickname(),
            composition.commentCreatedAt()
        );
    }
}
```

---

### Step 1.2: MyGroupMomentResponse.java 생성

**파일 경로**: `server/src/main/java/moment/moment/dto/response/MyGroupMomentResponse.java`

**참고 파일**:
- `moment/dto/response/GroupMomentResponse.java` (기본 필드)
- `moment/dto/response/MomentNotificationResponse.java` (알림 필드)

**구현 내용**:
```java
package moment.moment.dto.response;

import moment.moment.domain.Moment;
import java.time.LocalDateTime;
import java.util.List;

public record MyGroupMomentResponse(
    Long momentId,
    String content,
    String memberNickname,
    Long memberId,
    long likeCount,
    boolean hasLiked,
    long commentCount,
    LocalDateTime createdAt,
    List<MyGroupMomentCommentResponse> comments,          // 추가
    MomentNotificationResponse momentNotification         // 추가 (재사용)
) {
    public static MyGroupMomentResponse of(
            Moment moment,
            long likeCount,
            boolean hasLiked,
            long commentCount,
            List<MyGroupMomentCommentResponse> comments,
            MomentNotificationResponse momentNotification
    ) {
        return new MyGroupMomentResponse(
            moment.getId(),
            moment.getContent(),
            moment.getMember() != null ? moment.getMember().getNickname() : null,
            moment.getMember() != null ? moment.getMember().getId() : null,
            likeCount,
            hasLiked,
            commentCount,
            moment.getCreatedAt(),
            comments,
            momentNotification
        );
    }
}
```

---

### Step 1.3: MyGroupFeedResponse.java 생성

**파일 경로**: `server/src/main/java/moment/moment/dto/response/MyGroupFeedResponse.java`

**참고 파일**: `moment/dto/response/GroupFeedResponse.java` (기존 패턴)

**구현 내용**:
```java
package moment.moment.dto.response;

import java.util.List;

public record MyGroupFeedResponse(
    List<MyGroupMomentResponse> moments,
    Long nextCursor,
    boolean hasNextPage
) {
    public static MyGroupFeedResponse of(List<MyGroupMomentResponse> moments, Long nextCursor) {
        return new MyGroupFeedResponse(moments, nextCursor, nextCursor != null);
    }

    public static MyGroupFeedResponse empty() {
        return new MyGroupFeedResponse(List.of(), null, false);
    }
}
```

---

### Step 2: MyGroupMomentPageFacadeService.java 생성

**파일 경로**: `server/src/main/java/moment/moment/service/facade/MyGroupMomentPageFacadeService.java`

**참고 파일**:
- `moment/service/facade/MyMomentPageFacadeService.java` (배치 조회 패턴)
- `moment/service/facade/CommentableMomentFacadeService.java` (그룹 기반 패턴)

**의존성**:
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyGroupMomentPageFacadeService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final MomentService momentService;
    private final GroupMemberService groupMemberService;
    private final CommentService commentService;
    private final CommentApplicationService commentApplicationService;
    private final NotificationApplicationService notificationApplicationService;
    private final MomentLikeService momentLikeService;

    // ...
}
```

#### 2.1 getMyMomentsInGroup() 구현

```java
public MyGroupFeedResponse getMyMomentsInGroup(Long groupId, Long userId, Long cursor) {
    // 1. 그룹 멤버 검증
    GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

    // 2. 모멘트 조회 (커서 기반 페이징)
    List<Moment> moments = momentService.getMyMomentsInGroup(
        groupId, member.getId(), cursor, DEFAULT_PAGE_SIZE);

    if (moments.isEmpty()) {
        return MyGroupFeedResponse.empty();
    }

    List<Long> momentIds = moments.stream().map(Moment::getId).toList();

    // 3. 배치 조회
    // 3-1. 댓글 조회 (momentId별 그룹화)
    List<CommentComposition> allComments = commentApplicationService.getMyCommentCompositionsBy(momentIds);
    Map<Long, List<CommentComposition>> commentsMap = allComments.stream()
            .collect(Collectors.groupingBy(CommentComposition::momentId));

    // 3-2. 알림 조회
    Map<Long, List<Long>> notificationsMap =
        notificationApplicationService.getNotificationsByTargetIdsAndTargetType(
            momentIds, TargetType.MOMENT);

    // 4. 응답 조합
    List<MyGroupMomentResponse> responses = moments.stream()
            .map(moment -> createMyGroupMomentResponse(moment, member.getId(), commentsMap, notificationsMap))
            .toList();

    // 5. 다음 커서 계산
    Long nextCursor = moments.size() < DEFAULT_PAGE_SIZE ? null : moments.get(moments.size() - 1).getId();

    return MyGroupFeedResponse.of(responses, nextCursor);
}

private MyGroupMomentResponse createMyGroupMomentResponse(
        Moment moment,
        Long memberId,
        Map<Long, List<CommentComposition>> commentsMap,
        Map<Long, List<Long>> notificationsMap
) {
    Long momentId = moment.getId();

    // 좋아요 정보
    long likeCount = momentLikeService.getCount(momentId);
    boolean hasLiked = momentLikeService.hasLiked(momentId, memberId);

    // 댓글 정보
    List<CommentComposition> compositions = commentsMap.getOrDefault(momentId, List.of());
    List<MyGroupMomentCommentResponse> comments = compositions.stream()
            .map(MyGroupMomentCommentResponse::from)
            .toList();
    long commentCount = comments.size();

    // 알림 정보
    List<Long> notificationIds = notificationsMap.getOrDefault(momentId, List.of());
    MomentNotificationResponse notification = MomentNotificationResponse.from(notificationIds);

    return MyGroupMomentResponse.of(
        moment, likeCount, hasLiked, commentCount, comments, notification);
}
```

#### 2.2 getUnreadMyMomentsInGroup() 구현

**확인 완료**:
- ✅ `NotificationApplicationService.getUnreadNotifications(userId, TargetType)` 메서드 **존재** (라인 73-75)
- ❌ `MomentService.getUnreadMyMomentsInGroup()` 메서드 **없음** - 추가 필요

```java
public MyGroupFeedResponse getUnreadMyMomentsInGroup(Long groupId, Long userId, Long cursor) {
    // 1. 그룹 멤버 검증
    GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

    // 2. 읽지 않은 알림의 모멘트 ID 목록 조회
    // ✅ 이 메서드는 이미 존재함
    List<Long> unreadMomentIds = notificationApplicationService.getUnreadNotifications(
        userId, TargetType.MOMENT);

    if (unreadMomentIds == null || unreadMomentIds.isEmpty()) {
        return MyGroupFeedResponse.empty();
    }

    // 3. 그룹 내 본인의 unread 모멘트만 필터링하여 조회
    // ⚠️ 이 메서드가 없으면 MomentService에 추가 필요
    List<Moment> moments = momentService.getUnreadMyMomentsInGroup(
        groupId, member.getId(), unreadMomentIds, cursor, DEFAULT_PAGE_SIZE);

    if (moments.isEmpty()) {
        return MyGroupFeedResponse.empty();
    }

    // 4. 이후 로직은 getMyMomentsInGroup()과 동일
    List<Long> momentIds = moments.stream().map(Moment::getId).toList();

    List<CommentComposition> allComments = commentApplicationService.getMyCommentCompositionsBy(momentIds);
    Map<Long, List<CommentComposition>> commentsMap = allComments.stream()
            .collect(Collectors.groupingBy(CommentComposition::momentId));

    Map<Long, List<Long>> notificationsMap =
        notificationApplicationService.getNotificationsByTargetIdsAndTargetType(
            momentIds, TargetType.MOMENT);

    List<MyGroupMomentResponse> responses = moments.stream()
            .map(moment -> createMyGroupMomentResponse(moment, member.getId(), commentsMap, notificationsMap))
            .toList();

    Long nextCursor = moments.size() < DEFAULT_PAGE_SIZE ? null : moments.get(moments.size() - 1).getId();

    return MyGroupFeedResponse.of(responses, nextCursor);
}
```

**추가 구현 필요 메서드**:

##### NotificationApplicationService - 이미 존재함 ✅
```java
// 위치: notification/service/application/NotificationApplicationService.java (라인 73-75)
// 이 메서드는 이미 구현되어 있음!
public List<Long> getUnreadNotifications(Long userId, TargetType targetType) {
    return notificationService.getUnreadTargetIdsBy(userId, targetType);
}
```

##### MomentService 추가 메서드 - 필요 ❌
```java
// 위치: moment/service/moment/MomentService.java
public List<Moment> getUnreadMyMomentsInGroup(
        Long groupId, Long memberId, List<Long> momentIds, Long cursor, int limit) {
    PageRequest pageable = PageRequest.of(0, limit);
    if (cursor == null) {
        return momentRepository.findByGroupIdAndMemberIdAndIdIn(
            groupId, memberId, momentIds, pageable);
    }
    return momentRepository.findByGroupIdAndMemberIdAndIdInAndIdLessThan(
        groupId, memberId, momentIds, cursor, pageable);
}
```

##### MomentRepository 추가 쿼리
```java
// 위치: moment/infrastructure/MomentRepository.java
@Query("""
    SELECT m FROM moments m
    JOIN FETCH m.momenter
    LEFT JOIN FETCH m.member
    WHERE m.group.id = :groupId
      AND m.member.id = :memberId
      AND m.id IN :momentIds
    ORDER BY m.id DESC
""")
List<Moment> findByGroupIdAndMemberIdAndIdIn(
    @Param("groupId") Long groupId,
    @Param("memberId") Long memberId,
    @Param("momentIds") List<Long> momentIds,
    Pageable pageable);

@Query("""
    SELECT m FROM moments m
    JOIN FETCH m.momenter
    LEFT JOIN FETCH m.member
    WHERE m.group.id = :groupId
      AND m.member.id = :memberId
      AND m.id IN :momentIds
      AND m.id < :cursor
    ORDER BY m.id DESC
""")
List<Moment> findByGroupIdAndMemberIdAndIdInAndIdLessThan(
    @Param("groupId") Long groupId,
    @Param("memberId") Long memberId,
    @Param("momentIds") List<Long> momentIds,
    @Param("cursor") Long cursor,
    Pageable pageable);
```

---

### Step 3: GroupMomentController.java 수정

**파일 경로**: `server/src/main/java/moment/group/presentation/GroupMomentController.java`

**현재 상태 (라인 113-121)**:
```java
@GetMapping("/my-moments")
public ResponseEntity<SuccessResponse<GroupFeedResponse>> getMyMoments(
        @AuthenticationPrincipal Authentication authentication,
        @PathVariable Long groupId,
        @RequestParam(required = false) Long cursor) {
    GroupFeedResponse response = momentApplicationService.getMyMomentsInGroup(
        groupId, authentication.id(), cursor);
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}
```

#### 3.1 의존성 추가

```java
// 기존 의존성 유지
private final MomentApplicationService momentApplicationService;
private final MomentLikeService momentLikeService;
private final CommentableMomentFacadeService commentableMomentFacadeService;

// 신규 추가
private final MyGroupMomentPageFacadeService myGroupMomentPageFacadeService;
```

#### 3.2 getMyMoments() 수정

```java
@Operation(summary = "그룹 내 나의 모멘트 조회",
           description = "그룹 내에서 자신이 작성한 모멘트를 조회합니다. 댓글과 알림 정보가 포함됩니다.")
@GetMapping("/my-moments")
public ResponseEntity<SuccessResponse<MyGroupFeedResponse>> getMyMoments(
        @AuthenticationPrincipal Authentication authentication,
        @PathVariable Long groupId,
        @RequestParam(required = false) Long cursor) {
    MyGroupFeedResponse response = myGroupMomentPageFacadeService.getMyMomentsInGroup(
        groupId, authentication.id(), cursor);
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}
```

#### 3.3 getUnreadMyMoments() 신규 추가

```java
@Operation(summary = "그룹 내 읽지 않은 나의 모멘트 조회",
           description = "그룹 내에서 알림을 읽지 않은 자신의 모멘트를 조회합니다.")
@GetMapping("/my-moments/unread")
public ResponseEntity<SuccessResponse<MyGroupFeedResponse>> getUnreadMyMoments(
        @AuthenticationPrincipal Authentication authentication,
        @PathVariable Long groupId,
        @RequestParam(required = false) Long cursor) {
    MyGroupFeedResponse response = myGroupMomentPageFacadeService.getUnreadMyMomentsInGroup(
        groupId, authentication.id(), cursor);
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}
```

---

### Step 4: 테스트 작성

> **기존 프로젝트 테스트 패턴 분석 결과 반영**
> - Facade 테스트: `@SpringBootTest` + `@MockitoBean` (Hybrid 패턴)
> - E2E 테스트: `@Tag(TestTags.E2E)` + RestAssured + DatabaseCleaner
> - 메서드 네이밍: 한글 + 언더스코어 (`@DisplayNameGeneration(ReplaceUnderscores.class)`)

#### 4.1 Facade 서비스 테스트 (Hybrid 패턴)

**파일 경로**: `server/src/test/java/moment/moment/service/facade/MyGroupMomentPageFacadeServiceTest.java`

**참고 패턴**: `NotificationFacadeServiceTest.java`

```java
@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@DisplayNameGeneration(ReplaceUnderscores.class)
class MyGroupMomentPageFacadeServiceTest {

    @Autowired
    private MyGroupMomentPageFacadeService facadeService;

    // 실제 Repository (데이터 준비용)
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    // 테스트 픽스처
    private User user;
    private Group group;
    private GroupMember member;

    @BeforeEach
    void setUp() {
        user = userRepository.save(UserFixture.createUser());
        group = groupRepository.save(GroupFixture.createGroup(user));
        member = groupMemberRepository.save(GroupMemberFixture.createMember(group, user, "닉네임"));
    }

    @Test
    void 그룹_내_나의_모멘트를_조회한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMoment(group, member));
        commentRepository.save(CommentFixture.createComment(moment, member));

        // when
        MyGroupFeedResponse response = facadeService.getMyMomentsInGroup(
            group.getId(), user.getId(), null);

        // then
        assertAll(
            () -> assertThat(response.moments()).hasSize(1),
            () -> assertThat(response.moments().get(0).comments()).hasSize(1),
            () -> assertThat(response.moments().get(0).momentNotification()).isNotNull()
        );
    }

    @Test
    void 그룹_내_모멘트가_없으면_빈_응답을_반환한다() {
        // given
        // 모멘트 없음

        // when
        MyGroupFeedResponse response = facadeService.getMyMomentsInGroup(
            group.getId(), user.getId(), null);

        // then
        assertAll(
            () -> assertThat(response.moments()).isEmpty(),
            () -> assertThat(response.hasNextPage()).isFalse()
        );
    }

    @Test
    void 읽지_않은_알림이_있는_모멘트를_조회한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMoment(group, member));
        notificationRepository.save(NotificationFixture.createUnreadNotification(
            user, moment.getId(), TargetType.MOMENT));

        // when
        MyGroupFeedResponse response = facadeService.getUnreadMyMomentsInGroup(
            group.getId(), user.getId(), null);

        // then
        assertAll(
            () -> assertThat(response.moments()).hasSize(1),
            () -> assertThat(response.moments().get(0).momentNotification().isRead()).isFalse()
        );
    }

    @Test
    void 읽지_않은_알림이_없으면_빈_응답을_반환한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMoment(group, member));
        // 알림 없음 또는 모두 읽음 처리

        // when
        MyGroupFeedResponse response = facadeService.getUnreadMyMomentsInGroup(
            group.getId(), user.getId(), null);

        // then
        assertThat(response.moments()).isEmpty();
    }

    @Test
    void 커서_기반_페이지네이션이_동작한다() {
        // given
        List<Moment> moments = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            moments.add(momentRepository.save(MomentFixture.createMoment(group, member)));
        }

        // when - 첫 페이지
        MyGroupFeedResponse firstPage = facadeService.getMyMomentsInGroup(
            group.getId(), user.getId(), null);

        // then
        assertAll(
            () -> assertThat(firstPage.moments()).hasSize(10),  // DEFAULT_PAGE_SIZE
            () -> assertThat(firstPage.hasNextPage()).isTrue(),
            () -> assertThat(firstPage.nextCursor()).isNotNull()
        );

        // when - 두 번째 페이지
        MyGroupFeedResponse secondPage = facadeService.getMyMomentsInGroup(
            group.getId(), user.getId(), firstPage.nextCursor());

        // then
        assertAll(
            () -> assertThat(secondPage.moments()).hasSize(5),
            () -> assertThat(secondPage.hasNextPage()).isFalse()
        );
    }
}
```

#### 4.2 Controller E2E 테스트

**파일 경로**: `server/src/test/java/moment/group/presentation/GroupMomentControllerTest.java` (기존 파일에 추가)

**참고 패턴**: 기존 `GroupMomentControllerTest.java`의 다른 테스트 메서드

```java
// 기존 GroupMomentControllerTest 클래스에 추가할 테스트 메서드

@Test
void 그룹_내_나의_모멘트를_조회한다() {
    // given
    String token = 로그인_토큰_생성(user);
    GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "닉네임");
    모멘트_작성(token, group.groupId(), "첫 번째 모멘트");
    모멘트_작성(token, group.groupId(), "두 번째 모멘트");

    // when
    MyGroupFeedResponse response = RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .cookie("accessToken", token)
        .when().get("/api/v2/groups/{groupId}/my-moments", group.groupId())
        .then().log().all()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .jsonPath()
        .getObject("data", MyGroupFeedResponse.class);

    // then
    assertAll(
        () -> assertThat(response.moments()).hasSize(2),
        () -> assertThat(response.moments().get(0).comments()).isNotNull(),
        () -> assertThat(response.moments().get(0).momentNotification()).isNotNull()
    );
}

@Test
void 그룹_내_읽지_않은_나의_모멘트를_조회한다() {
    // given
    String token = 로그인_토큰_생성(user);
    GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "닉네임");
    GroupMomentResponse moment = 모멘트_작성(token, group.groupId(), "모멘트 내용");

    // 다른 사용자가 댓글 작성 (알림 생성)
    String otherToken = 로그인_토큰_생성(otherUser);
    그룹_가입(otherToken, group.groupId(), "다른닉네임");
    댓글_작성(otherToken, moment.momentId(), "댓글 내용");

    // when
    MyGroupFeedResponse response = RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .cookie("accessToken", token)
        .when().get("/api/v2/groups/{groupId}/my-moments/unread", group.groupId())
        .then().log().all()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .jsonPath()
        .getObject("data", MyGroupFeedResponse.class);

    // then
    assertAll(
        () -> assertThat(response.moments()).hasSize(1),
        () -> assertThat(response.moments().get(0).momentNotification().isRead()).isFalse()
    );
}

@Test
void 그룹_멤버가_아니면_나의_모멘트_조회_시_예외가_발생한다() {
    // given
    String ownerToken = 로그인_토큰_생성(user);
    GroupCreateResponse group = 그룹_생성(ownerToken, "테스트 그룹", "설명", "닉네임");

    String nonMemberToken = 로그인_토큰_생성(nonMemberUser);

    // when & then
    RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .cookie("accessToken", nonMemberToken)
        .when().get("/api/v2/groups/{groupId}/my-moments", group.groupId())
        .then().log().all()
        .statusCode(HttpStatus.FORBIDDEN.value());  // 또는 적절한 에러 코드
}
```

#### 4.3 테스트 체크리스트

| 테스트 유형 | 파일 | 테스트 케이스 |
|-----------|------|-------------|
| **Facade 통합** | `MyGroupMomentPageFacadeServiceTest.java` | 5개 |
| **E2E** | `GroupMomentControllerTest.java` (추가) | 3개 |

**Facade 테스트 케이스**:
- [ ] `그룹_내_나의_모멘트를_조회한다()`
- [ ] `그룹_내_모멘트가_없으면_빈_응답을_반환한다()`
- [ ] `읽지_않은_알림이_있는_모멘트를_조회한다()`
- [ ] `읽지_않은_알림이_없으면_빈_응답을_반환한다()`
- [ ] `커서_기반_페이지네이션이_동작한다()`

**E2E 테스트 케이스**:
- [ ] `그룹_내_나의_모멘트를_조회한다()`
- [ ] `그룹_내_읽지_않은_나의_모멘트를_조회한다()`
- [ ] `그룹_멤버가_아니면_나의_모멘트_조회_시_예외가_발생한다()`

---

## Phase 2: 기존 API 삭제 (정리)

### Step 2.1: MomentControllerTest.java 테스트 삭제

**파일 경로**: `server/src/test/java/moment/moment/presentation/MomentControllerTest.java`

**삭제 대상 (5개 메서드)**:
| 메서드명 | 라인 범위 |
|----------|-----------|
| `내_모멘트를_등록_시간_순으로_정렬한_페이지를_조회한다()` | 211-256 |
| `내_모멘트_조회_시_읽음_상태를_함께_반환한다()` | 259-317 |
| `내_모멘트_조회_시_모멘트_태그가_없는_경우도_조회된다()` | 320-346 |
| `DB에_저장된_Moment가_limit보다_적을_경우_남은_목록을_반환한다()` | 350-393 |
| `나의_Moment_목록을_조회한다()` | 555-593 |

**삭제 대상 import**:
```java
import moment.moment.dto.response.tobe.MyMomentPageResponse;
import moment.moment.dto.response.tobe.MyMomentResponse;
```

---

### Step 2.2: MomentController.java 메서드 삭제

**파일 경로**: `server/src/main/java/moment/moment/presentation/MomentController.java`

**삭제 대상**:
1. **필드 제거** (라인 37):
   ```java
   private final MyMomentPageFacadeService myMomentPageFacadeService;
   ```

2. **import 제거** (라인 17, 20):
   ```java
   import moment.moment.dto.response.tobe.MyMomentPageResponse;
   import moment.moment.service.facade.MyMomentPageFacadeService;
   ```

3. **메서드 삭제**:
   - `readMyMoment()` (라인 90-115) - Swagger 문서 포함
   - `readUnreadMyMoment()` (라인 117-142) - Swagger 문서 포함

---

### Step 2.3: Facade 서비스 삭제

**삭제 파일**: `server/src/main/java/moment/moment/service/facade/MyMomentPageFacadeService.java`

---

### Step 2.4: DTO 파일 삭제 (4개)

**삭제 파일 목록**:
1. `server/src/main/java/moment/moment/dto/response/tobe/MyMomentPageResponse.java`
2. `server/src/main/java/moment/moment/dto/response/tobe/MyMomentResponse.java`
3. `server/src/main/java/moment/moment/dto/response/tobe/MyMomentsResponse.java`
4. `server/src/main/java/moment/moment/dto/response/MyMomentCommentResponse.java`

---

## 검증 계획

### 1. 빌드 & 테스트
```bash
cd server
./gradlew clean build    # 전체 빌드
./gradlew fastTest       # 빠른 테스트 (e2e 제외)
```

### 2. 수동 테스트

**내 모멘트 조회**:
```bash
curl -X GET "http://localhost:8080/api/v2/groups/1/my-moments" \
  -H "Authorization: Bearer {token}"
```

**읽지 않은 내 모멘트 조회**:
```bash
curl -X GET "http://localhost:8080/api/v2/groups/1/my-moments/unread" \
  -H "Authorization: Bearer {token}"
```

**예상 응답**:
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "moments": [
      {
        "momentId": 1,
        "content": "...",
        "memberNickname": "...",
        "memberId": 1,
        "likeCount": 5,
        "hasLiked": true,
        "commentCount": 3,
        "createdAt": "...",
        "comments": [
          {
            "id": 1,
            "content": "댓글 내용",
            "memberNickname": "작성자",
            "memberId": 2,
            "createdAt": "..."
          }
        ],
        "momentNotification": {
          "isRead": false,
          "notificationIds": [101, 102]
        }
      }
    ],
    "nextCursor": 0,
    "hasNextPage": false
  }
}
```

---

## 최종 파일 변경 요약

### Phase 1 - 신규 생성 (4개)
| 파일 | 설명 |
|------|------|
| `MyGroupMomentCommentResponse.java` | 그룹 모멘트 댓글 응답 DTO |
| `MyGroupMomentResponse.java` | 그룹 내 나의 모멘트 응답 DTO |
| `MyGroupFeedResponse.java` | 피드 응답 래퍼 |
| `MyGroupMomentPageFacadeService.java` | Facade 서비스 |

### Phase 1 - 수정 (3개)
| 파일 | 변경 내용 |
|------|-----------|
| `GroupMomentController.java` | 의존성 추가, 2개 엔드포인트 (수정 + 신규) |
| `NotificationApplicationService.java` | `getUnreadTargetIds()` 메서드 추가 |
| `MomentService.java` + `MomentRepository.java` | unread 모멘트 조회 메서드 추가 |

### Phase 2 - 삭제 (5개)
| 파일 | 이유 |
|------|------|
| `MyMomentPageFacadeService.java` | deprecated API 전용 |
| `MyMomentPageResponse.java` | deprecated API 전용 |
| `MyMomentResponse.java` | deprecated API 전용 |
| `MyMomentsResponse.java` | deprecated API 전용 |
| `MyMomentCommentResponse.java` | deprecated API 전용 |

### Phase 2 - 수정 (2개)
| 파일 | 변경 내용 |
|------|-----------|
| `MomentController.java` | deprecated 메서드 2개 삭제 |
| `MomentControllerTest.java` | deprecated 테스트 5개 삭제 |

---

## 구현 체크리스트

### Phase 1
- [ ] Step 1.1: MyGroupMomentCommentResponse.java 생성
- [ ] Step 1.2: MyGroupMomentResponse.java 생성
- [ ] Step 1.3: MyGroupFeedResponse.java 생성
- [ ] Step 2: MyGroupMomentPageFacadeService.java 생성
  - [ ] getMyMomentsInGroup() 구현
  - [ ] getUnreadMyMomentsInGroup() 구현
  - [ ] (필요시) NotificationApplicationService에 메서드 추가
  - [ ] (필요시) MomentService + Repository에 메서드 추가
- [ ] Step 3: GroupMomentController.java 수정
  - [ ] 의존성 추가
  - [ ] getMyMoments() 수정
  - [ ] getUnreadMyMoments() 신규 추가
- [ ] Step 4: 테스트 작성
- [ ] 빌드 & 테스트 통과 확인

### Phase 2
- [ ] Step 2.1: MomentControllerTest.java 테스트 삭제
- [ ] Step 2.2: MomentController.java 메서드 삭제
- [ ] Step 2.3: MyMomentPageFacadeService.java 삭제
- [ ] Step 2.4: DTO 파일 4개 삭제
- [ ] 빌드 & 테스트 통과 확인
