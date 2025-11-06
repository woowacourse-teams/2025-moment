package moment.notification.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.domain.TargetType;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.infrastructure.NotificationRepository;
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
class NotificationServiceTest {

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
        user = UserFixture.createUser();
        userRepository.save(user);

        anotherUser = UserFixture.createUser();
        userRepository.save(anotherUser);
    }

    @Test
    void 알림을_저장한다() {
        // given
        Long contentId = 1L;
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;
        TargetType contentType = TargetType.MOMENT;

        // when
        Notification savedNotification = notificationService.saveNotification(user, contentId, reason, contentType);

        // then
        assertAll(
                () -> assertThat(savedNotification.getUser()).isEqualTo(user),
                () -> assertThat(savedNotification.getTargetId()).isEqualTo(contentId),
                () -> assertThat(savedNotification.getNotificationType()).isEqualTo(reason),
                () -> assertThat(savedNotification.getTargetType()).isEqualTo(contentType),
                () -> assertThat(savedNotification.isRead()).isFalse()
        );
    }

    @Test
    void 읽지_않은_알림의_타겟_ID들을_조회한다() {
        // given
        Long contentId = 1L;

        Long anotherContentId1 = 2L;
        Long anotherContentId2 = 3L;
        Long anotherContentId3 = 4L;

        TargetType contentType = TargetType.MOMENT;
        TargetType anotherType = TargetType.COMMENT;

        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;
        NotificationType anotherReason = NotificationType.NEW_REPLY_ON_COMMENT;

        Notification expectedNotification1 = new Notification(user, reason, contentType, contentId);
        notificationRepository.save(expectedNotification1);

        Notification readNotification = new Notification(user, reason, contentType, anotherContentId1);
        readNotification.checkNotification();
        notificationRepository.save(readNotification);

        Notification anotherTypeNotification = new Notification(user, anotherReason, anotherType, anotherContentId2);
        notificationRepository.save(anotherTypeNotification);

        Notification anotherUserNotification = new Notification(anotherUser, reason, contentType, anotherContentId3);
        notificationRepository.save(anotherUserNotification);

        // when
        List<Long> unreadTargetIds = notificationService.getUnreadTargetIdsBy(user.getId(), contentType);

        // then
        assertAll(
                () -> assertThat(unreadTargetIds).hasSize(1),
                () -> assertThat(unreadTargetIds).contains(contentId)
        );
    }

    @Test
    void 타겟_ID와_읽음_여부로_알림_목록을_조회한다() {
        // given
        Long contentId1 = 1L;
        Long contentId2 = 2L;
        Long anotherContentId = 3L;
        TargetType targetType = TargetType.MOMENT;
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;

        Notification unreadNotification1 = new Notification(user, reason, targetType, contentId1);
        Notification savedUnreadNotification1 = notificationRepository.save(unreadNotification1);

        Notification unreadNotification2 = new Notification(user, reason, targetType, contentId2);
        Notification savedUnreadNotification2 = notificationRepository.save(unreadNotification2);

        Notification readNotification = new Notification(user, reason, targetType, contentId1);
        readNotification.checkNotification();
        Notification savedReadNotification = notificationRepository.save(readNotification);

        Notification anotherContentIdNotification = new Notification(user, reason, targetType, anotherContentId);
        notificationRepository.save(anotherContentIdNotification);

        List<Long> contentIds = List.of(contentId1, contentId2);

        // when
        List<Notification> unreadNotifications = notificationService.getNotificationsBy(contentIds, false, targetType);
        List<Notification> readNotifications = notificationService.getNotificationsBy(contentIds, true, targetType);

        // then
        assertAll(
                () -> assertThat(unreadNotifications).hasSize(2),
                () -> assertThat(unreadNotifications).containsExactly(savedUnreadNotification1,
                        savedUnreadNotification2),
                () -> assertThat(readNotifications).hasSize(1),
                () -> assertThat(readNotifications).containsExactlyInAnyOrder(savedReadNotification)
        );
    }

    @Test
    void 사용자_ID와_읽음_여부로_모든_알림을_조회한다() {
        // given
        Long contentId1 = 1L;
        Long contentId2 = 2L;
        Long anotherContentId = 3L;
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;
        TargetType contentType = TargetType.MOMENT;

        Notification userUnreadNotification1 = new Notification(user, reason, contentType, contentId1);
        notificationRepository.save(userUnreadNotification1);

        Notification userUnreadNotification2 = new Notification(user, reason, contentType, contentId2);
        notificationRepository.save(userUnreadNotification2);

        Notification readNotification = new Notification(user, reason, contentType, anotherContentId);
        readNotification.checkNotification();
        notificationRepository.save(readNotification);

        Notification anotherUserNotification = new Notification(anotherUser, reason, contentType, contentId1);
        notificationRepository.save(anotherUserNotification);

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
                new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT, 1L));
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
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;
        TargetType contentType = TargetType.MOMENT;

        Notification notification1 = notificationRepository.save(new Notification(user, reason, contentType, 1L));
        Notification notification2 = notificationRepository.save(new Notification(user, reason, contentType, 2L));

        List<Long> notificationIds = List.of(notification1.getId(), notification2.getId());

        // when
        notificationService.markAllAsRead(notificationIds);

        // then
        List<Notification> updatedNotifications = notificationRepository.findAllById(notificationIds);
        assertThat(updatedNotifications).allMatch(Notification::isRead);
    }
}
