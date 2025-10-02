package moment.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import moment.notification.domain.PushNotification;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class PushNotificationRepositoryTest {

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
        String deviceToken = "another-test-device-token";
        PushNotification pushNotification = new PushNotification(user, deviceToken);

        // when
        PushNotification savedNotification = pushNotificationRepository.save(pushNotification);

        // then
        assertThat(savedNotification.getId()).isNotNull();
        assertThat(savedNotification.getUser()).isEqualTo(user);
        assertThat(savedNotification.getDeviceEndpoint()).isEqualTo(deviceToken);
    }

    @Test
    void 사용자_ID로_푸시_알림_정보를_성공적으로_조회한다() {
        // given
        String deviceToken = "test-device-token";
        PushNotification pushNotification = new PushNotification(user, deviceToken);
        pushNotificationRepository.save(pushNotification);

        // when
        List<PushNotification> foundNotification = pushNotificationRepository.findByUserId(user.getId());

        // then
        assertAll(
                () -> assertThat(foundNotification.getFirst().getUser()).isEqualTo(user),
                () -> assertThat(foundNotification.getFirst().getDeviceEndpoint()).isEqualTo(deviceToken)
        );
    }

    @Test
    void 사용자_ID에_해당하는_푸시_알림_정보가_없으면_빈_Optional을_반환한다() {
        // given
        Long nonExistentUserId = 999L;

        // when
        List<PushNotification> foundNotification = pushNotificationRepository.findByUserId(nonExistentUserId);

        // then
        assertThat(foundNotification).isEmpty();
    }
}
