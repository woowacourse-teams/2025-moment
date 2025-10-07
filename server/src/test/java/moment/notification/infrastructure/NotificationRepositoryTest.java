package moment.notification.infrastructure;

import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.global.domain.TargetType;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("lebron@james.com", "lebron1234!", "르브론", ProviderType.EMAIL);
        userRepository.save(user);
    }

    @Test
    void 읽지_않은_알림을_조회한다() {
        // given
        Notification notification1 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                1L);
        Notification notification2 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                1L);
        Notification notification3 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                1L);

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);

        // when
        List<Notification> notifications = notificationRepository.findAllByUserIdAndIsRead(user.getId(), false);

        // then
        assertThat(notifications).hasSize(3);
        assertThat(notifications.stream()
                .noneMatch(Notification::isRead))
                .isTrue();
    }

    @Test
    void 읽은_알림을_조회한다() {
        // given
        Notification notification1 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                1L);
        Notification notification2 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                1L);
        Notification notification3 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                1L);

        notification2.checkNotification();
        notification3.checkNotification();

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);

        // when
        List<Notification> notifications = notificationRepository.findAllByUserIdAndIsRead(user.getId(), true);

        // then
        assertThat(notifications).hasSize(2);
        assertThat(notifications.stream()
                .allMatch(Notification::isRead))
                .isTrue();
    }

    @Test
    void 읽지_않은_알림을_타겟_타입과_함께_조회한다() {
        // given
        Notification notification1 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                1L);
        Notification notification2 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                2L);
        Notification notification3 = new Notification(user, NotificationType.NEW_REPLY_ON_COMMENT, TargetType.COMMENT,
                1L);

        notification2.checkNotification();

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);

        // when
        List<Notification> notifications = notificationRepository.findAllByUserAndIsReadAndTargetType(user, false,
                TargetType.MOMENT);

        // then
        Assertions.assertAll(
                () -> assertThat(notifications).hasSize(1),
                () -> assertThat(notifications.stream()
                        .allMatch(notification -> !notification.isRead() && notification.getTargetType() == TargetType.MOMENT))
                        .isTrue()
        );
    }
}
