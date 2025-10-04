package moment.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.dto.request.NotificationReadRequest;
import moment.notification.dto.response.NotificationResponse;
import moment.notification.infrastructure.NotificationRepository;
import moment.notification.service.NotificationQueryService;
import moment.notification.service.NotificationService;
import moment.user.application.UserQueryService;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationQueryService notificationQueryService;

    @Test
    void 사용자가_읽지_않은_알림을_조회한다() {
        // given
        User user = new User("lebron@james.com", "james1234!", "르브론", ProviderType.EMAIL);
        Notification notification1 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                1L);
        Notification notification2 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                2L);
        Notification notification3 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                3L);

        given(userQueryService.getUserById(any(Long.class))).willReturn(user);
        given(notificationRepository.findAllByUserAndIsRead(any(User.class), eq(false)))
                .willReturn(List.of(notification1, notification2, notification3));
        // when
        List<NotificationResponse> responses = notificationService.getNotificationByUser(1L, false);

        // then
        assertThat(responses).hasSize(3);
    }

    @Test
    void 사용자가_읽은_알림을_조회한다() {
        // given
        User user = new User("lebron@james.com", "james1234!", "르브론", ProviderType.EMAIL);
        Notification notification1 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                1L);
        Notification notification2 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                2L);
        Notification notification3 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                3L);

        notification1.checkNotification();
        notification2.checkNotification();

        given(userQueryService.getUserById(any(Long.class))).willReturn(user);
        given(notificationRepository.findAllByUserAndIsRead(any(User.class), eq(true)))
                .willReturn(List.of(notification3));

        // when
        List<NotificationResponse> responses = notificationService.getNotificationByUser(1L, true);

        // then
        assertThat(responses).hasSize(1);
    }

    @Test
    void 사용자가_알림을_읽는다() {
        // given
        User user = new User("lebron@james.com", "james1234!", "르브론", ProviderType.EMAIL);
        Notification notification = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                1L);

        given(notificationQueryService.getNotificationById(any(Long.class))).willReturn(notification);

        // when
        notificationService.markAsRead(2L);

        // then
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void 사용자가_요청으로_들어온_알림을_읽는다() {
        // given
        User user = new User("lebron@james.com", "james1234!", "르브론", ProviderType.EMAIL);
        Notification notification1 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                1L);
        Notification notification2 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                2L);
        Notification notification3 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                3L);
        Notification notification4 = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                4L);
        NotificationReadRequest request = new NotificationReadRequest(List.of(1L, 2L, 3L, 4L));
        List<Notification> notifications = List.of(notification1, notification2, notification3, notification4);

        given(notificationQueryService.getNotificationsByIds(any()))
                .willReturn(notifications);

        // when
        notificationService.readNotifications(request);

        // then
        assertThat(notifications.stream().allMatch(Notification::isRead)).isTrue();
    }
}
