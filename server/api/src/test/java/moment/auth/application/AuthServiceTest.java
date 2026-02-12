package moment.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Date;
import java.util.List;
import moment.auth.domain.RefreshToken;
import moment.auth.infrastructure.RefreshTokenRepository;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.notification.domain.PushNotification;
import moment.notification.domain.PushNotificationSender;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PushNotificationRepository pushNotificationRepository;

    @MockitoBean
    private PushNotificationSender pushNotificationSender;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(UserFixture.createUser());
    }

    @Test
    void 로그아웃_시_디바이스_엔드포인트가_제공되면_해당_토큰이_삭제된다() {
        // given
        String deviceEndpoint = "test-device-token";
        RefreshToken refreshToken = new RefreshToken("refresh-token-value", user,
                new Date(), new Date(System.currentTimeMillis() + 100000));
        refreshTokenRepository.save(refreshToken);
        pushNotificationRepository.save(new PushNotification(user, deviceEndpoint));

        // when
        authService.logout(user.getId(), deviceEndpoint);

        // then
        List<PushNotification> notifications = pushNotificationRepository.findByUserId(user.getId());
        boolean refreshTokenExists = refreshTokenRepository.existsByUser(user);

        assertAll(
                () -> assertThat(notifications).isEmpty(),
                () -> assertThat(refreshTokenExists).isFalse()
        );
    }

    @Test
    void 로그아웃_시_디바이스_엔드포인트가_null이면_토큰은_유지된다() {
        // given
        String deviceEndpoint = "test-device-token";
        RefreshToken refreshToken = new RefreshToken("refresh-token-value", user,
                new Date(), new Date(System.currentTimeMillis() + 100000));
        refreshTokenRepository.save(refreshToken);
        pushNotificationRepository.save(new PushNotification(user, deviceEndpoint));

        // when
        authService.logout(user.getId(), null);

        // then
        List<PushNotification> notifications = pushNotificationRepository.findByUserId(user.getId());
        boolean refreshTokenExists = refreshTokenRepository.existsByUser(user);

        assertAll(
                () -> assertThat(notifications).hasSize(1),
                () -> assertThat(refreshTokenExists).isFalse()
        );
    }

    @Test
    void 로그아웃_시_디바이스_엔드포인트가_빈_문자열이면_토큰은_유지된다() {
        // given
        String deviceEndpoint = "test-device-token";
        RefreshToken refreshToken = new RefreshToken("refresh-token-value", user,
                new Date(), new Date(System.currentTimeMillis() + 100000));
        refreshTokenRepository.save(refreshToken);
        pushNotificationRepository.save(new PushNotification(user, deviceEndpoint));

        // when
        authService.logout(user.getId(), "");

        // then
        List<PushNotification> notifications = pushNotificationRepository.findByUserId(user.getId());
        assertAll(
                () -> assertThat(notifications).hasSize(1),
                () -> assertThat(refreshTokenRepository.existsByUser(user)).isFalse()
        );
    }

    @Test
    void 로그아웃_시_디바이스_엔드포인트_없이_호출하면_토큰은_유지된다() {
        // given
        String deviceEndpoint = "test-device-token";
        RefreshToken refreshToken = new RefreshToken("refresh-token-value", user,
                new Date(), new Date(System.currentTimeMillis() + 100000));
        refreshTokenRepository.save(refreshToken);
        pushNotificationRepository.save(new PushNotification(user, deviceEndpoint));

        // when
        authService.logout(user.getId());

        // then
        List<PushNotification> notifications = pushNotificationRepository.findByUserId(user.getId());
        boolean refreshTokenExists = refreshTokenRepository.existsByUser(user);

        assertAll(
                () -> assertThat(notifications).hasSize(1),
                () -> assertThat(refreshTokenExists).isFalse()
        );
    }
}
