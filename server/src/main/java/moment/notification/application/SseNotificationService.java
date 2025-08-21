package moment.notification.application;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import moment.global.logging.NoLogging;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
public class SseNotificationService {

    private static final long VALID_TIME = 10 * 60 * 1000L;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(VALID_TIME);

        emitters.put(userId, emitter);

        emitter.onError(e -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onCompletion(() -> emitters.remove(userId));

        sendToClient(userId, "connect", "Connection success");

        return emitter;
    }

    public void sendToClient(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }

    @Scheduled(fixedRate = 25_000)
    @NoLogging
    public void sendHeartbeat() {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .comment("keeping connection alive")
                        .data("heartbeat"));
                log.info("Sent heartbeat to user {}", userId);
            } catch (IOException e) {
                log.info("User {} connection lost. Removing emitter.", userId);
                emitters.remove(userId);
            }
        });
    }
}
