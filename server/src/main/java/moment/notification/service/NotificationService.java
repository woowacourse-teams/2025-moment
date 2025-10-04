package moment.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.dto.request.NotificationReadRequest;
import moment.notification.dto.response.NotificationResponse;
import moment.notification.infrastructure.NotificationRepository;
import moment.user.application.tobe.user.UserService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final UserService userService;
    private final NotificationRepository notificationRepository;
    private final NotificationQueryService notificationQueryService;

    public List<NotificationResponse> getNotificationByUser(Long id, Boolean read) {
        User user = userService.getUserById(id);
        List<Notification> notifications = notificationRepository.findAllByUserAndIsRead(user, read);

        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public Notification saveNotification(User momenter,
                                         Long momentId,
                                         NotificationType notificationType,
                                         TargetType targetType
    ) {
        Notification notificationWithoutId = new Notification(momenter, notificationType, targetType, momentId);

        return notificationRepository.save(notificationWithoutId);
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationQueryService.getNotificationById(id);
        notification.checkNotification();
    }

    @Transactional
    public void readNotifications(NotificationReadRequest notificationReadRequest) {
        List<Long> notificationIds = notificationReadRequest.notificationIds();
        List<Notification> notifications = notificationQueryService.getNotificationsByIds(notificationIds);

        notifications.forEach(Notification::checkNotification);
    }
}
