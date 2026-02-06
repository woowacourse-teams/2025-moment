package moment.notification.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Map;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.SourceData;
import moment.notification.infrastructure.NotificationRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        user = UserFixture.createUser();
        userRepository.save(user);

        anotherUser = UserFixture.createUser();
        userRepository.save(anotherUser);
    }

    @AfterEach
    void down() {
        databaseCleaner.clean();
    }

    @Test
    void 알림을_저장한다() {
        // given
        NotificationType notificationType = NotificationType.NEW_COMMENT_ON_MOMENT;
        SourceData sourceData = SourceData.of(Map.of("momentId", 1L));
        String link = "/moments/1";

        // when
        Notification savedNotification = notificationService.save(
                user, notificationType, sourceData, link);

        // then
        assertAll(
                () -> assertThat(savedNotification.getUser()).isEqualTo(user),
                () -> assertThat(savedNotification.getNotificationType()).isEqualTo(notificationType),
                () -> assertThat(savedNotification.getSourceData()).isEqualTo(sourceData),
                () -> assertThat(savedNotification.getLink()).isEqualTo(link),
                () -> assertThat(savedNotification.isRead()).isFalse()
        );
    }

    @Test
    void notification_type_목록으로_사용자의_알림을_조회한다() {
        // given
        notificationRepository.save(new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 1L)), "/moments/1"));
        notificationRepository.save(new Notification(user,
                NotificationType.COMMENT_LIKED,
                SourceData.of(Map.of("commentId", 2L)), "/comments/2"));

        List<NotificationType> momentTypes = List.of(
                NotificationType.NEW_COMMENT_ON_MOMENT, NotificationType.MOMENT_LIKED);

        // when
        List<Notification> result = notificationService.getAllBy(user.getId(), false, momentTypes);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void 사용자_ID와_읽음_여부로_모든_알림을_조회한다() {
        // given
        notificationRepository.save(new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 1L)), "/moments/1"));
        notificationRepository.save(new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 2L)), "/moments/2"));

        Notification readNotification = new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 3L)), "/moments/3");
        readNotification.markAsRead();
        notificationRepository.save(readNotification);

        notificationRepository.save(new Notification(anotherUser,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 1L)), "/moments/1"));

        // when
        List<Notification> unreadNotifications = notificationService.getAllBy(user.getId(), false);
        List<Notification> readNotifications = notificationService.getAllBy(user.getId(), true);

        // then
        assertAll(
                () -> assertThat(unreadNotifications).hasSize(2),
                () -> assertThat(readNotifications).hasSize(1)
        );
    }

    @Test
    void 알림을_읽음_처리한다() {
        // given
        Notification notification = notificationRepository.save(
                new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT,
                        SourceData.of(Map.of("momentId", 1L)), "/moments/1"));
        assertThat(notification.isRead()).isFalse();

        // when
        notificationService.markAsRead(notification.getId());

        // then
        Notification updatedNotification = notificationRepository.findById(notification.getId()).get();
        assertThat(updatedNotification.isRead()).isTrue();
    }

    @Test
    void 존재하지_않는_알림을_읽음_처리하면_예외가_발생한다() {
        // given
        Long nonExistedNotificationId = 999L;

        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(nonExistedNotificationId))
                .isInstanceOf(MomentException.class)
                .hasMessage(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    void 여러_알림을_한번에_읽음_처리한다() {
        // given
        Notification notification1 = notificationRepository.save(new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 1L)), "/moments/1"));
        Notification notification2 = notificationRepository.save(new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 2L)), "/moments/2"));

        List<Long> notificationIds = List.of(notification1.getId(), notification2.getId());

        // when
        notificationService.markAllAsRead(notificationIds);

        // then
        List<Notification> updatedNotifications = notificationRepository.findAllById(notificationIds);
        assertThat(updatedNotifications).allMatch(Notification::isRead);
    }
}
