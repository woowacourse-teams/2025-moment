package moment.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Map;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.SourceData;
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
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;
    private User anotherUser;
    private long userId;

    @BeforeEach
    void setUp() {
        user = UserFixture.createUser();
        userRepository.save(user);
        userId = user.getId();

        anotherUser = UserFixture.createUser();
        userRepository.save(anotherUser);
    }

    @Test
    void user_id를_고려하여_읽지_않은_알림들을_조회한다() {
        // given
        Notification expectedNotification = new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 1L)), "/moments/1");

        Notification readNotification = new Notification(user,
                NotificationType.COMMENT_LIKED,
                SourceData.of(Map.of("commentId", 1L)), "/comments/1");
        readNotification.markAsRead();

        Notification anotherUserNotification = new Notification(anotherUser,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 1L)), "/moments/1");

        notificationRepository.saveAll(
                List.of(expectedNotification, readNotification, anotherUserNotification));

        // when
        List<Notification> unreadNotifications = notificationRepository.findAllByUserIdAndIsRead(userId, false);

        // then
        assertAll(
                () -> assertThat(unreadNotifications).hasSize(1),
                () -> assertThat(unreadNotifications.stream()
                        .noneMatch(Notification::isRead)).isTrue()
        );
    }

    @Test
    void user_id를_고려하여_읽은_알림들을_조회한다() {
        // given
        Notification unreadNotification = new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 1L)), "/moments/1");

        Notification readNotification = new Notification(user,
                NotificationType.COMMENT_LIKED,
                SourceData.of(Map.of("commentId", 1L)), "/comments/1");
        readNotification.markAsRead();

        Notification anotherUserNotification = new Notification(anotherUser,
                NotificationType.COMMENT_LIKED,
                SourceData.of(Map.of("commentId", 1L)), "/comments/1");
        anotherUserNotification.markAsRead();

        notificationRepository.saveAll(
                List.of(unreadNotification, readNotification, anotherUserNotification));

        // when
        List<Notification> readNotifications = notificationRepository.findAllByUserIdAndIsRead(userId, true);

        // then
        assertAll(
                () -> assertThat(readNotifications).hasSize(1),
                () -> assertThat(readNotifications.stream()
                        .allMatch(Notification::isRead)).isTrue()
        );
    }

    @Test
    void notification_type_목록으로_읽지_않은_알림을_조회한다() {
        // given
        Notification momentNotification = new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 42L)), "/moments/42");
        Notification likeNotification = new Notification(user,
                NotificationType.MOMENT_LIKED,
                SourceData.of(Map.of("momentId", 43L)), "/moments/43");
        Notification commentNotification = new Notification(user,
                NotificationType.COMMENT_LIKED,
                SourceData.of(Map.of("commentId", 10L)), "/comments/10");
        Notification readNotification = new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 44L)), "/moments/44");
        readNotification.markAsRead();

        notificationRepository.saveAll(List.of(
                momentNotification, likeNotification, commentNotification, readNotification));

        List<NotificationType> momentTypes = List.of(
                NotificationType.NEW_COMMENT_ON_MOMENT, NotificationType.MOMENT_LIKED);

        // when
        List<Notification> result = notificationRepository
                .findAllByUserIdAndIsReadAndNotificationTypeIn(userId, false, momentTypes);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void notification_type_목록으로_모든_읽지_않은_알림을_조회한다() {
        // given
        Notification userNotification = new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 42L)), "/moments/42");
        Notification anotherUserNotification = new Notification(anotherUser,
                NotificationType.MOMENT_LIKED,
                SourceData.of(Map.of("momentId", 43L)), "/moments/43");
        Notification commentNotification = new Notification(user,
                NotificationType.COMMENT_LIKED,
                SourceData.of(Map.of("commentId", 10L)), "/comments/10");

        notificationRepository.saveAll(List.of(
                userNotification, anotherUserNotification, commentNotification));

        List<NotificationType> momentTypes = List.of(
                NotificationType.NEW_COMMENT_ON_MOMENT, NotificationType.MOMENT_LIKED);

        // when
        List<Notification> result = notificationRepository
                .findAllByIsReadAndNotificationTypeIn(false, momentTypes);

        // then
        assertThat(result).hasSize(2);
    }
}
