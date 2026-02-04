package moment.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import moment.config.TestTags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    private Map<Long, List<SseEmitter>> spyEmitters;

    @BeforeEach
    void setUp() {
        spyEmitters = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(emitters, "emitters", spyEmitters);
    }

    @Test
    void 사용자에게_SseEmitter를_추가한다() {
        // given
        Long userId = 1L;
        SseEmitter mockEmitter = mock(SseEmitter.class);

        // when
        emitters.add(userId, mockEmitter);

        // then
        assertThat(spyEmitters.containsKey(userId)).isTrue();
        assertThat(spyEmitters.get(userId)).hasSize(1);
        then(mockEmitter).should(times(1)).onCompletion(any(Runnable.class));
        then(mockEmitter).should(times(1)).onTimeout(any(Runnable.class));
        then(mockEmitter).should(times(1)).onError(any(Consumer.class));
    }

    @Test
    void 같은_사용자에게_여러_SseEmitter를_추가할_수_있다() {
        // given
        Long userId = 1L;
        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);

        // when
        emitters.add(userId, emitter1);
        emitters.add(userId, emitter2);

        // then
        assertThat(spyEmitters.get(userId)).hasSize(2);
    }

    @Test
    void 특정_emitter_에러_시_해당_emitter만_제거한다() {
        // given
        Long userId = 1L;
        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);

        List<SseEmitter> userEmitters = new CopyOnWriteArrayList<>();
        userEmitters.add(emitter1);
        userEmitters.add(emitter2);
        spyEmitters.put(userId, userEmitters);

        // when - simulate removing emitter1
        userEmitters.remove(emitter1);

        // then
        assertThat(spyEmitters.get(userId)).hasSize(1);
        assertThat(spyEmitters.get(userId)).contains(emitter2);
    }

    @Test
    void 사용자의_모든_emitter에_메시지를_전송한다() throws IOException {
        // given
        Long userId = 1L;
        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);
        List<SseEmitter> userEmitters = new CopyOnWriteArrayList<>();
        userEmitters.add(emitter1);
        userEmitters.add(emitter2);
        spyEmitters.put(userId, userEmitters);

        // when
        emitters.sendToClient(userId, "testEvent", "testData");

        // then
        then(emitter1).should(times(1)).send(any(SseEventBuilder.class));
        then(emitter2).should(times(1)).send(any(SseEventBuilder.class));
    }

    @Test
    void 전송_실패한_emitter는_자동_제거된다() throws IOException {
        // given
        Long userId = 1L;
        SseEmitter goodEmitter = mock(SseEmitter.class);
        SseEmitter badEmitter = mock(SseEmitter.class);
        doThrow(new IOException("connection lost")).when(badEmitter).send(any(SseEventBuilder.class));

        List<SseEmitter> userEmitters = new CopyOnWriteArrayList<>();
        userEmitters.add(goodEmitter);
        userEmitters.add(badEmitter);
        spyEmitters.put(userId, userEmitters);

        // when
        emitters.sendToClient(userId, "testEvent", "testData");

        // then
        assertThat(spyEmitters.get(userId)).hasSize(1);
        assertThat(spyEmitters.get(userId)).contains(goodEmitter);
    }

    @Test
    void 모든_사용자의_emitter에_하트비트를_전송한다() throws IOException {
        // given
        Long userId1 = 1L;
        Long userId2 = 2L;
        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);
        spyEmitters.put(userId1, new CopyOnWriteArrayList<>(List.of(emitter1)));
        spyEmitters.put(userId2, new CopyOnWriteArrayList<>(List.of(emitter2)));

        // when
        emitters.sendHeartbeat();

        // then
        then(emitter1).should(times(1)).send(any(SseEventBuilder.class));
        then(emitter2).should(times(1)).send(any(SseEventBuilder.class));
    }

    @Test
    void 마지막_emitter_제거_시_사용자_엔트리를_삭제한다() throws IOException {
        // given
        Long userId = 1L;
        SseEmitter badEmitter = mock(SseEmitter.class);
        doThrow(new IOException("connection lost")).when(badEmitter).send(any(SseEventBuilder.class));

        List<SseEmitter> userEmitters = new CopyOnWriteArrayList<>();
        userEmitters.add(badEmitter);
        spyEmitters.put(userId, userEmitters);

        // when
        emitters.sendToClient(userId, "testEvent", "testData");

        // then
        assertThat(spyEmitters.containsKey(userId)).isFalse();
    }
}
