package moment.notification.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.notification.domain.Notification;
import moment.notification.dto.response.NotificationResponse;
import moment.notification.infrastructure.NotificationRepository;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final UserQueryService userQueryService;
    private final NotificationRepository notificationRepository;
    private final NotificationQueryService notificationQueryService;

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
