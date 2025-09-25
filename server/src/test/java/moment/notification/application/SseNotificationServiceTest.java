package moment.notification.application;

import static org.assertj.core.api.Assertions.assertThat;

import moment.notification.domain.Emitters;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
public class SseNotificationServiceTest {

    @InjectMocks
    SseNotificationService sseNotificationService;

    @Mock
    Emitters emitters;

    @Test
    void 사용자가_구독하면_emitter가_생성된다() {
        // given
        SseEmitter emitter = sseNotificationService.subscribe(1L);

        // when & then
        assertThat(emitter).isNotNull();
    }
}
