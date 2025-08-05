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
import moment.comment.dto.request.CommentCreateRequest;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.notification.domain.NotificationType;
import moment.notification.domain.TargetType;
import moment.notification.dto.response.NotificationResponse;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import okhttp3.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class NotificationControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private ObjectMapper objectMapper;

    private User momenter;
    private User actor; // 코멘트 작성자 또는 이모지 반응자가 될 사용자
    private Moment moment;
    private String momenterToken;
    private String actorToken;

    // 1. 공통 Given 로직 추출
    @BeforeEach
    void setUp() {
        momenter = userRepository.save(new User("lebron@james.com", "moment1234!", "르브론"));
        actor = userRepository.save(new User("curry@stephan.com", "moment1234!", "커리"));
        moment = momentRepository.save(new Moment("나의 재능을 Miami로", momenter));

        momenterToken = tokenManager.createToken(momenter.getId(), momenter.getEmail());
        actorToken = tokenManager.createToken(actor.getId(), actor.getEmail());
    }


    @Test
    void 사용자가_내_모멘트에_코멘트를_달면_SSE_알림을_받는다() throws InterruptedException {
        // when
        List<NotificationResponse> receivedNotifications = new CopyOnWriteArrayList<>();

        // 1. SSE 클라이언트를 사용하여 비동기 구독
        EventSource eventSource = subscribeToNotifications(momenterToken, receivedNotifications);

        CommentCreateRequest request = new CommentCreateRequest("굿~", moment.getId());
        RestAssured.given().log().all()
                .cookie("token", actorToken) // 코멘트 작성자로 인증
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/comments")
                .then().log().all()
                .statusCode(201);

        // then
        // Awaitility를 사용해 비동기적으로 도착하는 알림을 기다립니다.
        await().atMost(2, TimeUnit.SECONDS).until(() -> !receivedNotifications.isEmpty());
        NotificationResponse response = receivedNotifications.get(0);

        assertAll(
                () -> assertThat(receivedNotifications).hasSize(1),
                () -> assertThat(response.notificationType()).isEqualTo(NotificationType.NEW_COMMENT_ON_MOMENT),
                () -> assertThat(response.targetType()).isEqualTo(TargetType.MOMENT),
                () -> assertThat(response.targetId()).isEqualTo(moment.getId()),
                () -> assertThat(response.message()).isEqualTo("내 모멘트에 새로운 코멘트가 달렸습니다."),
                () -> assertThat(response.isRead()).isFalse()
        );

        eventSource.close();
    }


    //SSE 구독 로직
    private EventSource subscribeToNotifications(String token, List<NotificationResponse> notificationList) {
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
                NotificationResponse notification = objectMapper.readValue(messageEvent.getData(),
                        NotificationResponse.class);
                notificationList.add(notification);
            }
        };

        Headers headers = Headers.of("Cookie", "token=" + token);
        EventSource eventSource = new EventSource.Builder(eventHandler,
                URI.create("http://localhost:" + 8080 + "/api/v1/notifications/subscribe"))
                .headers(headers)
                .build();

        eventSource.start();

        // SSE 연결이 수립될 시간을 약간 기다려주는 것이 안정적입니다.
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return eventSource;
    }
}
