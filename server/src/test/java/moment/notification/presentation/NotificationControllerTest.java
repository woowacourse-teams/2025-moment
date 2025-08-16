package moment.notification.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import moment.auth.application.TokenManager;
import moment.comment.domain.Comment;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.infrastructure.CommentRepository;
import moment.common.DatabaseCleaner;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.TargetType;
import moment.notification.dto.response.NotificationResponse;
import moment.notification.dto.response.NotificationSseResponse;
import moment.notification.infrastructure.NotificationRepository;
import moment.reply.application.EmojiService;
import moment.reply.dto.request.EmojiCreateRequest;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.infrastructure.UserRepository;
import okhttp3.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayNameGeneration(ReplaceUnderscores.class)
public class NotificationControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private User momenter;
    private Moment moment;
    private Moment moment2;
    private Moment moment3;
    private String momenterToken;
    private User commenter;
    private String commenterToken;
    @Autowired
    private EmojiService emojiService;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        momenter = userRepository.save(new User("lebron@james.com", "moment1234!", "르브론", ProviderType.EMAIL));
        moment = momentRepository.save(new Moment("나의 재능을 Miami로", momenter));
        moment2 = momentRepository.save(new Moment("안녕하세요", momenter));
        moment3 = momentRepository.save(new Moment("반가워요", momenter));
        momenterToken = tokenManager.createAccessToken(momenter.getId(), momenter.getEmail());
        commenter = userRepository.save(new User("curry@stephan.com", "moment1234!", "커리", ProviderType.EMAIL));
        commenterToken = tokenManager.createAccessToken(commenter.getId(), commenter.getEmail());
    }

    @Test
    void 사용자가_내_모멘트에_코멘트를_달면_SSE_알림을_받는다() throws InterruptedException {
        // when
        List<NotificationSseResponse> receivedNotifications = new CopyOnWriteArrayList<>();

        EventSource eventSource = subscribeToNotifications(momenterToken, receivedNotifications);

        CommentCreateRequest request = new CommentCreateRequest("굿~", moment.getId());
        RestAssured.given().log().all()
                .cookie("token", commenterToken) // 코멘트 작성자로 인증
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/comments")
                .then().log().all()
                .statusCode(201);

        // then
        await().atMost(2, TimeUnit.SECONDS).until(() -> !receivedNotifications.isEmpty());
        NotificationSseResponse response = receivedNotifications.get(0);

        assertAll(
                () -> assertThat(receivedNotifications).hasSize(1),
                () -> assertThat(response.notificationType()).isEqualTo(NotificationType.NEW_COMMENT_ON_MOMENT),
                () -> assertThat(response.targetType()).isEqualTo(TargetType.MOMENT),
                () -> assertThat(response.targetId()).isEqualTo(moment.getId()),
                () -> assertThat(response.message()).isEqualTo(NotificationType.NEW_COMMENT_ON_MOMENT.getMessage()),
                () -> assertThat(response.isRead()).isFalse()
        );

        eventSource.close();
    }

    @Test
    void 사용자가_코멘트에_반응을_달면_SSE_알림을_받는다() throws InterruptedException {
        // when
        List<NotificationSseResponse> receivedNotifications = new CopyOnWriteArrayList<>();

        EventSource eventSource = subscribeToNotifications(commenterToken, receivedNotifications);
        Comment comment = commentRepository.save(new Comment("하하", commenter, moment));

        EmojiCreateRequest request = new EmojiCreateRequest("HEART", comment.getId());
        RestAssured.given().log().all()
                .cookie("token", momenterToken) // 모멘트 작성자가 이모지를 달음
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/emojis")
                .then().log().all()
                .statusCode(201);

        // then
        await().atMost(2, TimeUnit.SECONDS).until(() -> !receivedNotifications.isEmpty());
        NotificationSseResponse response = receivedNotifications.get(0);

        assertAll(
                () -> assertThat(receivedNotifications).hasSize(1),
                () -> assertThat(response.notificationType()).isEqualTo(NotificationType.NEW_REPLY_ON_COMMENT),
                () -> assertThat(response.targetType()).isEqualTo(TargetType.COMMENT),
                () -> assertThat(response.targetId()).isEqualTo(comment.getId()),
                () -> assertThat(response.message()).isEqualTo(NotificationType.NEW_REPLY_ON_COMMENT.getMessage()),
                () -> assertThat(response.isRead()).isFalse()
        );

        eventSource.close();
    }

    @Test
    void 사용자가_읽지_않은_모멘트_알림을_받는다() {
        // given
        CommentCreateRequest request1 = new CommentCreateRequest("굿~", moment.getId());
        CommentCreateRequest request2 = new CommentCreateRequest("굿~", moment2.getId());
        CommentCreateRequest request3 = new CommentCreateRequest("굿~", moment3.getId());

        // when
        RestAssured.given().log().all()
                .cookie("token", commenterToken)
                .contentType(ContentType.JSON)
                .body(request1)
                .when().post("/api/v1/comments")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", commenterToken)
                .contentType(ContentType.JSON)
                .body(request2)
                .when().post("/api/v1/comments")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", commenterToken)
                .contentType(ContentType.JSON)
                .body(request3)
                .when().post("/api/v1/comments")
                .then().log().all()
                .statusCode(201);

        List<NotificationResponse> responses = RestAssured.given().log().all()
                .cookie("token", momenterToken)
                .when().get("/api/v1/notifications?read=false")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath()
                .getList("data", NotificationResponse.class);

        // then
        assertAll(
                () -> assertThat(responses).hasSize(3),
                () -> assertThat(responses.stream()
                        .noneMatch(NotificationResponse::isRead))
                        .isTrue());
    }

    @Test
    void 사용자가_읽지_않은_코멘트_알림을_받는다() {
        // given
        Comment comment = commentRepository.save(new Comment("하하", commenter, moment2));
        EmojiCreateRequest request1 = new EmojiCreateRequest("HEART", comment.getId());
        EmojiCreateRequest request2 = new EmojiCreateRequest("DDABONG", comment.getId());
        EmojiCreateRequest request3 = new EmojiCreateRequest("STAR", comment.getId());
        EmojiCreateRequest request4 = new EmojiCreateRequest("KING", comment.getId());

        Authentication authentication = new Authentication(momenter.getId());

        emojiService.addEmoji(request1, authentication);
        emojiService.addEmoji(request2, authentication);
        emojiService.addEmoji(request3, authentication);
        emojiService.addEmoji(request4, authentication);

        List<NotificationResponse> responses = RestAssured.given().log().all()
                .cookie("token", commenterToken)
                .when().get("/api/v1/notifications?read=false")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath()
                .getList("data", NotificationResponse.class);

        // then
        assertAll(
                () -> assertThat(responses).hasSize(4),
                () -> assertThat(responses.stream()
                        .noneMatch(NotificationResponse::isRead))
                        .isTrue());
    }

    @Test
    void 사용자가_알림을_확인한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("굿~", moment.getId());

        // when
        RestAssured.given().log().all()
                .cookie("token", commenterToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/comments")
                .then().log().all()
                .statusCode(201);

        Notification notification = notificationRepository.findAllByUserAndIsRead(momenter, false).getFirst();

        RestAssured.given().log().all()
                .cookie("token", momenterToken)
                .contentType(ContentType.JSON)
                .when().patch("/api/v1/notifications/" + notification.getId() + "/read")
                .then().log().all()
                .statusCode(204);

        Notification readNotification = notificationRepository.findById(notification.getId()).get();
        System.out.println(readNotification.getId());

        // then
        assertThat(readNotification.isRead()).isTrue();
    }


    //SSE 구독 로직
    private EventSource subscribeToNotifications(String token, List<NotificationSseResponse> notificationList) {
        EventHandler eventHandler = new EventHandler() {
            @Override
            public void onOpen() {
            }

            @Override
            public void onClosed() {
            }

            @Override
            public void onComment(String comment) {
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onMessage(String event, MessageEvent messageEvent) throws Exception {
                NotificationSseResponse notification = objectMapper.readValue(messageEvent.getData(),
                        NotificationSseResponse.class);
                notificationList.add(notification);
            }
        };

        Headers headers = Headers.of("Cookie", "token=" + token);
        EventSource eventSource = new EventSource.Builder(eventHandler,
                URI.create("http://localhost:" + 8080 + "/api/v1/notifications/subscribe"))
                .headers(headers)
                .build();

        eventSource.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return eventSource;
    }
}
