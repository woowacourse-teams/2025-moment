package moment.notification.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Optional;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.infrastructure.NotificationRepository;
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
class DefaultNotificationQueryServiceTest {

    @InjectMocks
    private DefaultNotificationQueryService defaultNotificationQueryService;

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    void 알림을_ID로_조회한다() {
        // given
        User user = new User("gg@gmail.com", "1234abd!", "gg", ProviderType.EMAIL);
        Notification notification = new Notification(user, NotificationType.NEW_REPLY_ON_COMMENT, TargetType.MOMENT,
                1L);
        given(notificationRepository.findById(any(Long.class))).willReturn(Optional.of(notification));

        // when
        defaultNotificationQueryService.getNotificationById(1L);

        // then
        then(notificationRepository).should(times(1)).findById(any(Long.class));
    }

    @Test
    void 알림들을_ID들로_조회한다() {
        // given
        User user = new User("gg@gmail.com", "1234abd!", "gg", ProviderType.EMAIL);
        Notification notification1 = new Notification(user, NotificationType.NEW_REPLY_ON_COMMENT, TargetType.MOMENT,
                1L);
        Notification notification2 = new Notification(user, NotificationType.NEW_REPLY_ON_COMMENT, TargetType.MOMENT,
                2L);
        Notification notification3 = new Notification(user, NotificationType.NEW_REPLY_ON_COMMENT, TargetType.MOMENT,
                3L);
        Notification notification4 = new Notification(user, NotificationType.NEW_REPLY_ON_COMMENT, TargetType.MOMENT,
                4L);

        given(notificationRepository.findAllById(any()))
                .willReturn(List.of(notification1, notification2, notification3, notification4));

        // when
        defaultNotificationQueryService.getNotificationsByIds(List.of(1L, 2L, 3L, 4L));

        // then
        then(notificationRepository).should(times(1)).findAllById(any());
    }
}
