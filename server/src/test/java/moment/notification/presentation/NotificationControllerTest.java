package moment.notification.presentation;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.restassured.RestAssured;
import java.util.List;
import moment.auth.application.TokenManager;
import moment.comment.dto.request.CommentCreateRequest;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;

import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.dto.request.NotificationReadRequest;
import moment.notification.dto.response.NotificationResponse;
import moment.notification.dto.response.NotificationSseResponse;
import moment.notification.infrastructure.NotificationRepository;
import moment.notification.service.application.NotificationApplicationService;
import moment.notification.service.notification.SseNotificationService;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(TestTags.E2E)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(ReplaceUnderscores.class)
public class NotificationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private NotificationApplicationService notificationApplicationService;

    @MockitoBean
    private SseNotificationService sseNotificationService;

    private User momenter;
    private Moment moment;
    private Moment moment2;
    private Moment moment3;
    private String momenterToken;
    private User commenter;
    private String commenterToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        momenter = userRepository.save(UserFixture.createUser());
        moment = momentRepository.save(new Moment("나의 재능을 Miami로", momenter));
        moment2 = momentRepository.save(new Moment("안녕하세요", momenter));
        moment3 = momentRepository.save(new Moment("반가워요", momenter));
        momenterToken = tokenManager.createAccessToken(momenter.getId(), momenter.getEmail());
        commenter = userRepository.save(UserFixture.createUser());
        commenterToken = tokenManager.createAccessToken(commenter.getId(), commenter.getEmail());
    }

    @AfterEach
    void databaseClean() {
        databaseCleaner.clean();
    }

    @Test
    void 사용자가_내_모멘트에_코멘트를_달면_SSE_알림을_받는다() {
        // given
        given(sseNotificationService.subscribe(anyLong())).willReturn(new SseEmitter());

        // when
        CommentCreateRequest request = new CommentCreateRequest("굿~", moment.getId(), null, null);
        RestAssured.given().log().all()
                .cookie("accessToken", commenterToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(request)
                .when().post("/api/v2/comments")
                .then().log().all()
                .statusCode(201);

        // then
        ArgumentCaptor<NotificationSseResponse> responseCaptor = ArgumentCaptor.forClass(NotificationSseResponse.class);
        await().atMost(2, SECONDS).untilAsserted(() -> {
            then(sseNotificationService).should()
                    .sendToClient(eq(momenter.getId()), eq("notification"), responseCaptor.capture());
        });

        NotificationSseResponse response = responseCaptor.getValue();

        assertAll(
                () -> assertThat(response.notificationType()).isEqualTo(NotificationType.NEW_COMMENT_ON_MOMENT),
                () -> assertThat(response.message()).isEqualTo(NotificationType.NEW_COMMENT_ON_MOMENT.getMessage()),
                () -> assertThat(response.link()).isNull()
        );
    }

    @Test
    void 사용자가_읽지_않은_모멘트_알림을_받는다() {
        // given
        CommentCreateRequest request1 = new CommentCreateRequest("굿~", moment.getId(), null, null);
        CommentCreateRequest request2 = new CommentCreateRequest("굿~", moment2.getId(), null, null);
        CommentCreateRequest request3 = new CommentCreateRequest("굿~", moment3.getId(), null, null);

        // when
        RestAssured.given().log().all()
                .cookie("accessToken", commenterToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(request1)
                .when().post("/api/v2/comments")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("accessToken", commenterToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(request2)
                .when().post("/api/v2/comments")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("accessToken", commenterToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(request3)
                .when().post("/api/v2/comments")
                .then().log().all()
                .statusCode(201);

        // then
        await().atMost(2, SECONDS).untilAsserted(() -> {
            List<NotificationResponse> responses = RestAssured.given().log().all()
                    .cookie("accessToken", momenterToken)
                    .when().get("/api/v2/notifications?read=false")
                    .then().log().all()
                    .statusCode(200)
                    .extract().jsonPath()
                    .getList("data", NotificationResponse.class);

            assertAll(
                    () -> assertThat(responses).hasSize(3),
                    () -> assertThat(responses.stream()
                            .noneMatch(NotificationResponse::isRead))
                            .isTrue());
        });
    }

    @Test
    void 사용자가_알림을_확인한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("굿~", moment.getId(), null, null);

        // when
        RestAssured.given().log().all()
                .cookie("accessToken", commenterToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(request)
                .when().post("/api/v2/comments")
                .then().log().all()
                .statusCode(201);

        await().atMost(2, SECONDS).until(() ->
                notificationRepository.findAllByUserIdAndIsRead(momenter.getId(), false).size() == 1);

        Notification notification = notificationRepository.findAllByUserIdAndIsRead(momenter.getId(), false).getFirst();

        RestAssured.given().log().all()
                .cookie("accessToken", momenterToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .when().patch("/api/v2/notifications/" + notification.getId() + "/read")
                .then().log().all()
                .statusCode(204);

        Notification readNotification = notificationRepository.findById(notification.getId()).get();
        System.out.println(readNotification.getId());

        // then
        assertThat(readNotification.isRead()).isTrue();
    }

    @Test
    void 사용자가_알림들을_확인한다() {
        // given
        CommentCreateRequest request1 = new CommentCreateRequest("굿~", moment.getId(), null, null);
        CommentCreateRequest request2 = new CommentCreateRequest("굿~", moment2.getId(), null, null);

        RestAssured.given().log().all()
                .cookie("accessToken", commenterToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(request1)
                .when().post("/api/v2/comments")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("accessToken", commenterToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(request2)
                .when().post("/api/v2/comments")
                .then().log().all()
                .statusCode(201);

        await().atMost(2, SECONDS).until(() ->
                notificationRepository.findAllByUserIdAndIsRead(
                        momenter.getId(), false).size() == 2);

        List<Long> unReadNotificationsIds = notificationRepository.findAllByUserIdAndIsRead(
                momenter.getId(), false).stream().map(Notification::getId).toList();

        NotificationReadRequest notificationReadRequest = new NotificationReadRequest(unReadNotificationsIds);

        // when
        RestAssured.given().log().all()
                .cookie("accessToken", momenterToken)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(notificationReadRequest)
                .when().patch("/api/v2/notifications/read-all")
                .then().log().all()
                .statusCode(204);

        List<Notification> results = notificationRepository.findAllById(unReadNotificationsIds);

        // then
        assertThat(results.stream().allMatch(Notification::isRead)).isTrue();
    }
}
