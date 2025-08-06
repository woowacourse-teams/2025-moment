package moment.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.TargetType;
import moment.notification.dto.response.NotificationResponse;
import moment.notification.dto.response.NotificationSseResponse;
import moment.notification.infrastructure.NotificationRepository;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    void 사용자가_구독하면_emitter가_생성된다() {
        // given
        SseEmitter emitter = notificationService.subscribe(1L);

        // when & then
        assertThat(emitter).isNotNull();
    }

    @Test
    void 클라이언트에게_알림을_전송한다() throws IOException {
        // given
        Long userId = 1L;
        SseEmitter mockEmitter = mock(SseEmitter.class);

        Map<Long, SseEmitter> emitters =
                (Map<Long, SseEmitter>) ReflectionTestUtils.getField(notificationService, "emitters");
        emitters.put(userId, mockEmitter);

        String eventName = "notification";
        NotificationSseResponse response = NotificationSseResponse.createSseResponse(
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT,
                1L
        );

        // when
        notificationService.sendToClient(userId, eventName, response);

        // then
        verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

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
        given(notificationRepository.findAllByUserAndIsRead(any(User.class), any(Boolean.class)))
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
        given(notificationRepository.findAllByUserAndIsRead(any(User.class), any(Boolean.class)))
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
        notificationService.markAsRead(1L);

        // then
        assertThat(notification.isRead()).isTrue();
    }
}
