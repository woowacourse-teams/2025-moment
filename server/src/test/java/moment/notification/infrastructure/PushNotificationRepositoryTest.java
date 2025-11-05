package moment.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.notification.domain.PushNotification;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@Tag(TestTags.INTEGRATION)
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
        user = UserFixture.createUser();
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

    @Test
    void 사용자의_디바이스_엔드포인트_존재_여부를_판단한다() {
        // given
        User anotherUser = UserFixture.createUser();
        userRepository.save(anotherUser);

        String existingDeviceEndpoint = "existing-device-endpoint";
        String nonExistingDeviceEndpoint = "non-existing-device-endpoint";

        PushNotification pushNotification = new PushNotification(user, existingDeviceEndpoint);
        PushNotification anotherUserPushNotification = new PushNotification(anotherUser, nonExistingDeviceEndpoint);

        pushNotificationRepository.save(pushNotification);
        pushNotificationRepository.save(anotherUserPushNotification);

        // when
        boolean shouldExist = pushNotificationRepository.existsByUserAndDeviceEndpoint(user, existingDeviceEndpoint);
        boolean shouldNotExist = pushNotificationRepository.existsByUserAndDeviceEndpoint(user,
                nonExistingDeviceEndpoint);

        // then
        assertThat(shouldExist).isTrue();
        assertThat(shouldNotExist).isFalse();
    }

    @Test
    void 사용자와_디바이스_엔드포인트로_푸시_알림_정보를_성공적으로_삭제한다() {
        // given
        String deviceToken = "test-device-token";
        String anotherToken = "another-test-device-token";

        PushNotification pushNotification = new PushNotification(user, deviceToken);
        pushNotificationRepository.save(pushNotification);

        PushNotification anotherPushNotification = new PushNotification(user, anotherToken);
        pushNotificationRepository.save(anotherPushNotification);

        // when
        pushNotificationRepository.deleteByUserAndDeviceEndpoint(user, deviceToken);

        // then
        List<PushNotification> foundNotification = pushNotificationRepository.findByUserId(user.getId());
        assertAll(
                () -> assertThat(foundNotification).hasSize(1),
                () -> assertThat(foundNotification.getFirst().getDeviceEndpoint()).isEqualTo(anotherToken)
        );
    }
}
