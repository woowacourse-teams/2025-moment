package moment.notification.application;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j // 임시로그
public class SseNotificationService {

    private static final long VALID_TIME = 10 * 60 * 1000L;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(VALID_TIME);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

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

//    @Scheduled(fixedRate = 45_000) // 45초마다 실행
//    public void sendHeartbeat() {
//        emitters.forEach((userId, emitter) -> {
//            try {
//                // comment 이벤트, 데이터는 현재 시간
//                emitter.send(SseEmitter.event()
//                        .name("heartbeat")
//                        .comment("keeping connection alive"));
//                log.info("Sent heartbeat to user {}", userId); // 임시로그
//            } catch (IOException e) {
//                // 연결이 끊겼다고 판단하고 emitter 제거
//                log.info("User {} connection lost. Removing emitter.", userId); // 임시로그
//                emitters.remove(userId);
//            }
//        });
//    }
}
