package moment.notification.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.notification.domain.PushNotification;
import moment.notification.infrastructure.PushNotificationRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class PushNotificationServiceTest {

    @Autowired
    private PushNotificationService pushNotificationService;

    @Autowired
    private PushNotificationRepository pushNotificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = UserFixture.createUser();
        userRepository.save(user);
    }

    @Test
    void 푸시_알림_정보를_성공적으로_저장한다() {
        // given
        String deviceEndpoint = "test-device-endpoint";

        // when
        pushNotificationService.save(user, deviceEndpoint);

        // then
        List<PushNotification> notifications = pushNotificationRepository.findByUserId(user.getId());
        assertAll(
                () -> assertThat(notifications).hasSize(1),
                () -> assertThat(notifications.get(0).getDeviceEndpoint()).isEqualTo(deviceEndpoint)
        );
    }

    @Test
    void 이미_존재하는_푸시_알림_정보는_저장하지_않는다() {
        // given
        String deviceEndpoint = "test-device-endpoint";
        pushNotificationService.save(user, deviceEndpoint);

        // when
        pushNotificationService.save(user, deviceEndpoint);

        // then
        List<PushNotification> notifications = pushNotificationRepository.findByUserId(user.getId());
        assertThat(notifications).hasSize(1);
    }

    @Test
    void 다른_사용자가_동일한_디바이스_토큰을_등록하면_기존_등록이_삭제된다() {
        // given
        User anotherUser = UserFixture.createUser();
        userRepository.save(anotherUser);

        String sharedToken = "shared-device-token";
        pushNotificationService.save(user, sharedToken);

        // when
        pushNotificationService.save(anotherUser, sharedToken);

        // then
        List<PushNotification> userNotifications = pushNotificationRepository.findByUserId(user.getId());
        List<PushNotification> anotherUserNotifications = pushNotificationRepository.findByUserId(anotherUser.getId());

        assertAll(
                () -> assertThat(userNotifications).isEmpty(),
                () -> assertThat(anotherUserNotifications).hasSize(1),
                () -> assertThat(anotherUserNotifications.get(0).getDeviceEndpoint()).isEqualTo(sharedToken)
        );
    }

    @Test
    void 사용자로_푸시_알림_정보를_성공적으로_삭제한다() {
        // given
        String deviceEndpoint = "test-device-endpoint";
        pushNotificationService.save(user, deviceEndpoint);

        // when
        pushNotificationService.deleteBy(user, deviceEndpoint);

        // then
        List<PushNotification> notifications = pushNotificationRepository.findByUserId(user.getId());
        assertThat(notifications).isEmpty();
    }
}
