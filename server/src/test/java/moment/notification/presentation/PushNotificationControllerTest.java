package moment.notification.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import moment.auth.infrastructure.JwtTokenManager;
import moment.common.DatabaseCleaner;
import moment.notification.domain.PushNotification;
import moment.notification.dto.request.DeviceEndPointRegisterRequest;
import moment.notification.infrastructure.PushNotificationRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayNameGeneration(ReplaceUnderscores.class)
class PushNotificationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PushNotificationRepository pushNotificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenManager jwtTokenManager;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private User user;
    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
        user = userRepository.save(new User("test@moment.com", "password", "tester", ProviderType.EMAIL));
        accessToken = jwtTokenManager.createAccessToken(user.getId(), user.getEmail());
    }

    @Test
    void 사용자_디바이스_정보_저장에_성공하면_DB에_해당_정보가_저장된다() {
        // given
        DeviceEndPointRegisterRequest request = new DeviceEndPointRegisterRequest("test-endpoint-arn");

        // when
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", accessToken)
            .body(request)
            .when().post("/api/v1/push-notifications")
            .then().log().all()
            .statusCode(HttpStatus.OK.value());

        // then
        List<PushNotification> notifications = pushNotificationRepository.findByUserId(user.getId());
        assertAll(
            () -> assertThat(notifications).hasSize(1),
            () -> assertThat(notifications.get(0).getDeviceEndpoint()).isEqualTo("test-endpoint-arn")
        );
    }

    @Test
    void 사용자_디바이스_정보_삭제에_성공하면_DB에서_해당_정보가_삭제된다() {
        // given
        pushNotificationRepository.save(new PushNotification(user, "test-endpoint-arn"));

        // when
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", accessToken)
            .when().delete("/api/v1/push-notifications")
            .then().log().all()
            .statusCode(HttpStatus.OK.value());

        // then
        List<PushNotification> notifications = pushNotificationRepository.findByUserId(user.getId());
        assertThat(notifications).isEmpty();
    }
}
