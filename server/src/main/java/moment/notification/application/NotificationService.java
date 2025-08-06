package moment.notification.application;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import moment.notification.domain.Notification;
import moment.notification.dto.response.NotificationResponse;
import moment.notification.infrastructure.NotificationRepository;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final long VALID_TIME = 10 * 60 * 1000L;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final UserQueryService userQueryService;
    private final NotificationRepository notificationRepository;
    private final NotificationQueryService notificationQueryService;

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(VALID_TIME);

        // Emitter 저장
        emitters.put(userId, emitter);

        // Emitter 완료 및 타임아웃 시 처리
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        // 연결 직후 503 방지를 위해 더미 데이터 전송
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

    public List<NotificationResponse> getNotificationByUser(Long id, Boolean read) {
        User user = userQueryService.getUserById(id);
        List<Notification> notifications = notificationRepository.findAllByUserAndIsRead(user, read);

        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationQueryService.getNotificationById(id);
        notification.checkNotification();
    }
}
