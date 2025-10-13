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
    private User notTargetUser;

    private final boolean isReadFlag = true;
    private final boolean isNotReadFlag = false;
    private final long contentId = 1L;

    @BeforeEach
    void setUp() {
        user = new User("lebron@james.com", "lebron1234!", "르브론", ProviderType.EMAIL);
        userRepository.save(user);

        notTargetUser = new User("mimi@icloud.com", "mimi1234!", "밍밍", ProviderType.EMAIL);
        userRepository.save(notTargetUser);
    }

    @Test
    void 읽지_않은_알림을_조회한다() {
        // given
        TargetType contentType = TargetType.MOMENT;
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;

        Notification unreadNotification1 = new Notification(user, reason, contentType, contentId);
        Notification unreadNotification2 = new Notification(user, reason, contentType, contentId);
        Notification unreadNotification3 = new Notification(user, reason, contentType, contentId);
        Notification notTargetUserNotification = new Notification(notTargetUser, reason, contentType, contentId);

        notificationRepository.save(unreadNotification1);
        notificationRepository.save(unreadNotification2);
        notificationRepository.save(unreadNotification3);
        notificationRepository.save(notTargetUserNotification);

        // when
        List<Notification> unreadNotifications = notificationRepository.findAllByUserAndIsRead(user, isNotReadFlag);

        // then
        assertThat(unreadNotifications).hasSize(3);
        assertThat(unreadNotifications.stream()
                .noneMatch(Notification::isRead))
                .isTrue();
    }

    @Test
    void 읽은_알림을_조회한다() {
        // given
        TargetType contentType = TargetType.MOMENT;
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;

        Notification unreadNotification1 = new Notification(user, reason, contentType, contentId);
        Notification readNotification1 = new Notification(user, reason, contentType, contentId);
        Notification readNotification2 = new Notification(user, reason, contentType, contentId);
        Notification notTargetUserNotification = new Notification(notTargetUser, reason, contentType, contentId);

        readNotification1.checkNotification();
        readNotification2.checkNotification();
        notTargetUserNotification.checkNotification();

        notificationRepository.save(unreadNotification1);
        notificationRepository.save(readNotification1);
        notificationRepository.save(readNotification2);
        notificationRepository.save(notTargetUserNotification);

        // when
        List<Notification> readNotifications = notificationRepository.findAllByUserAndIsRead(user, isReadFlag);

        // then
        assertThat(readNotifications).hasSize(2);
        assertThat(readNotifications.stream()
                .allMatch(Notification::isRead))
                .isTrue();
    }

    @Test
    void 읽지_않은_알림을_타겟_타입과_함께_조회한다() {
        // given
        TargetType targetContentType = TargetType.MOMENT;
        TargetType notTargetContentType = TargetType.COMMENT;
        NotificationType targetReason = NotificationType.NEW_COMMENT_ON_MOMENT;
        NotificationType notTargetReason = NotificationType.NEW_REPLY_ON_COMMENT;

        Notification expectedNotification = new Notification(user, targetReason, targetContentType, contentId);
        Notification readNotification = new Notification(user, targetReason, targetContentType, contentId);
        Notification notContentTypeNotification = new Notification(user, notTargetReason, notTargetContentType, contentId);
        Notification notTargetUserNotification = new Notification(notTargetUser, targetReason, targetContentType, contentId);

        readNotification.checkNotification();

        notificationRepository.save(expectedNotification);
        notificationRepository.save(readNotification);
        notificationRepository.save(notContentTypeNotification);
        notificationRepository.save(notTargetUserNotification);

        // when

        List<Notification> expectedNotifications = notificationRepository.findAllByUserAndIsReadAndTargetType(
                user, isNotReadFlag, targetContentType);

        // then
        Assertions.assertAll(
                () -> assertThat(expectedNotifications).hasSize(1),
                () -> assertThat(expectedNotifications.stream()
                        .allMatch(notification -> !notification.isRead()
                                && notification.getTargetType() == targetContentType))
                        .isTrue()
        );
    }
}
