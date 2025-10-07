package moment.notification.infrastructure;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
public class Emitters {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter add(Long userId, SseEmitter emitter) {
        emitters.put(userId, emitter);

        emitter.onError(e -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onCompletion(() -> emitters.remove(userId));

        return emitter;
    }

    public void remove(Long userId) {
        emitters.remove(userId);
    }

    public void sendToClient(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.error("Failed to send SSE event to user {}", userId, e);
                remove(userId);
            }
        }
    }

    public void sendHeartbeat() {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .comment("keeping connection alive")
                        .data("heartbeat"));
            } catch (IOException e) {
                log.info("User {} connection lost. Removing emitter.", userId);
                emitters.remove(userId);
            }
        });
    }
}
