package moment.notification.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import moment.notification.domain.PushNotification;
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
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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
        user = new User("test@moment.com", "password123!", "tester", ProviderType.EMAIL);
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
