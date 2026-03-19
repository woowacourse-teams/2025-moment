package moment.notification.infrastructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
public class Emitters {

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter add(Long userId, SseEmitter emitter) {
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onError(e -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onCompletion(() -> removeEmitter(userId, emitter));

        return emitter;
    }

    public void remove(Long userId) {
        List<SseEmitter> userEmitters = emitters.remove(userId);
        if (userEmitters != null) {
            for (SseEmitter emitter : userEmitters) {
                emitter.complete();
            }
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }

    public void sendToClient(Long userId, String eventName, Object data) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name(eventName).data(data));
                } catch (IOException e) {
                    log.error("Failed to send SSE event to user {}", userId, e);
                    deadEmitters.add(emitter);
                }
            }
            userEmitters.removeAll(deadEmitters);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }

    public void sendHeartbeat() {
        emitters.forEach((userId, userEmitters) -> {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .comment("keeping connection alive")
                            .data("heartbeat"));
                } catch (IOException e) {
                    log.info("User {} connection lost.", userId);
                    deadEmitters.add(emitter);
                }
            }
            userEmitters.removeAll(deadEmitters);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        });
    }
}
