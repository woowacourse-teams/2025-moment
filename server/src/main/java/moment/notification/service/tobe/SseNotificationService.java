package moment.notification.service.tobe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.global.logging.NoLogging;
import moment.notification.infrastructure.Emitters;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseNotificationService {

    private static final long VALID_TIME = 10 * 60 * 1000L;
    private final Emitters emitters;

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(VALID_TIME);
        emitters.add(userId, emitter);
        emitters.sendToClient(userId, "connect", "Connection success");

        return emitter;
    }

    public void sendToClient(Long userId, String eventName, Object data) {
        emitters.sendToClient(userId, eventName, data);
    }

    @Scheduled(fixedRate = 25_000)
    @NoLogging
    public void sendHeartbeat() {
        emitters.sendHeartbeat();
    }
}
