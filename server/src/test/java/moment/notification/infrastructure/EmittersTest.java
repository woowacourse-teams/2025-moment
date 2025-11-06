package moment.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import moment.config.TestTags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

@Tag(TestTags.UNIT)
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class EmittersTest {

    @InjectMocks
    private Emitters emitters;

    @Spy
    private Map<Long, SseEmitter> spyEmitters = new ConcurrentHashMap<>();

    @Mock
    private SseEmitter mockSseEmitter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emitters, "emitters", spyEmitters);
    }

    @Test
    void SseEmitter를_추가한다() {
        // given
        Long userId = 1L;

        // when
        emitters.add(userId, mockSseEmitter);

        // then
        assertThat(spyEmitters.containsKey(1L)).isTrue();
        then(mockSseEmitter).should(times(1)).onCompletion(any(Runnable.class));
        then(mockSseEmitter).should(times(1)).onTimeout(any(Runnable.class));
        then(mockSseEmitter).should(times(1)).onError(any(Consumer.class));
    }

    @Test
    void 클라이언트에게_데이터를_전송한다() throws IOException {
        // given
        Long userId = 1L;
        SseEmitter emitter = mock(SseEmitter.class);
        spyEmitters.put(userId, emitter);

        // when
        emitters.sendToClient(userId, "testEvent", "testData");

        // then
        then(emitter).should(times(1)).send(any(SseEventBuilder.class));
    }

    @Test
    void 모든_클라이언트에게_heartbeat를_전송한다() throws IOException {
        // given
        Long userId1 = 1L;
        Long userId2 = 2L;
        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);
        spyEmitters.put(userId1, emitter1);
        spyEmitters.put(userId2, emitter2);

        // when
        emitters.sendHeartbeat();

        // then
        then(emitter1).should(times(1)).send(any(SseEventBuilder.class));
        then(emitter2).should(times(1)).send(any(SseEventBuilder.class));
    }
}
