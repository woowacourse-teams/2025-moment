package moment.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import moment.global.domain.TargetType;
import moment.notification.service.NotificationFacade;
import moment.notification.service.NotificationService;
import moment.notification.service.tobe.SseNotificationService;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.dto.response.NotificationSseResponse;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationFacadeTest {

    @InjectMocks
    private NotificationFacade notificationFacade;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SseNotificationService sseNotificationService;

    @Test
    void 알림을_저장하고_SSE_알림을_전송한다() {
        // given
        final Long userId = 1L;
        final Long targetId = 100L;
        final Long notificationId = 200L;
        final NotificationType notificationType = NotificationType.NEW_COMMENT_ON_MOMENT;
        final TargetType targetType = TargetType.MOMENT;

        User user = mock(User.class);
        given(user.getId()).willReturn(userId);

        Notification savedNotification = mock(Notification.class);
        given(savedNotification.getId()).willReturn(notificationId);

        given(notificationService.saveNotification(user, targetId, notificationType, targetType))
                .willReturn(savedNotification);

        // when
        notificationFacade.sendSseNotificationAndNotification(user, targetId, notificationType, targetType);

        // then
        then(notificationService).should().saveNotification(user, targetId, notificationType, targetType);

        ArgumentCaptor<NotificationSseResponse> responseCaptor = ArgumentCaptor.forClass(NotificationSseResponse.class);
        then(sseNotificationService).should().sendToClient(eq(userId), eq("notification"), responseCaptor.capture());

        NotificationSseResponse capturedResponse = responseCaptor.getValue();
        assertAll(
                () -> assertThat(capturedResponse.notificationId()).isEqualTo(notificationId),
                () -> assertThat(capturedResponse.notificationType()).isEqualTo(notificationType),
                () -> assertThat(capturedResponse.targetType()).isEqualTo(targetType),
                () -> assertThat(capturedResponse.targetId()).isEqualTo(targetId)
        );
    }
}
