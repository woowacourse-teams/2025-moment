package moment.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Map;
import moment.notification.domain.NotificationType;
import moment.notification.domain.TargetType;
import moment.notification.dto.response.NotificationResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationServiceTest {

    private final NotificationService notificationService = new NotificationService();

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
        NotificationResponse response = NotificationResponse.createSseResponse(
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT,
                1L
        );

        // when
        notificationService.sendToClient(userId, eventName, response);

        // then
        verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

}
