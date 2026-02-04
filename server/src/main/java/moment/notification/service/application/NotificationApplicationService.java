package moment.notification.service.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.dto.request.NotificationReadRequest;
import moment.notification.dto.response.NotificationResponse;
import moment.notification.service.notification.NotificationService;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationApplicationService {

    private final NotificationService notificationService;
    private final UserService userService;

    public List<NotificationResponse> getNotificationBy(Long userId, boolean read) {
        userService.getUserBy(userId);

        List<Notification> notifications = notificationService.getAllBy(userId, read);

        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationService.markAsRead(notificationId);
    }

    @Transactional
    public void markAllAsRead(NotificationReadRequest notificationReadRequest) {
        List<Long> notificationIds = notificationReadRequest.notificationIds();
        notificationService.markAllAsRead(notificationIds);
    }

    @Transactional
    public Notification createNotification(
            Long userId,
            Long targetId,
            NotificationType notificationType,
            TargetType targetType,
            Long groupId) {

        User user = userService.getUserBy(userId);
        return notificationService.save(user, targetId, notificationType, targetType, groupId);
    }

    public List<Long> getUnreadNotifications(Long userId, TargetType targetType) {
        return notificationService.getUnreadTargetIdsBy(userId, targetType);
    }

    public Map<Long, List<Long>> getNotificationsByTargetIdsAndTargetType(List<Long> targetIds, TargetType targetType) {
        List<Notification> unreadNotificationIds = notificationService.getNotificationsBy(targetIds, false, targetType);

        return mapNotificationIdForMomentId(unreadNotificationIds, targetIds);
    }

    private Map<Long, List<Long>> mapNotificationIdForMomentId(List<Notification> unreadNotifications,
                                                               List<Long> momentIds) {

        Map<Long, List<Notification>> notificationsByMomentIds = unreadNotifications.stream()
                .collect(Collectors.groupingBy(Notification::getTargetId));

        return momentIds.stream()
                .collect(Collectors.toMap(
                        momentId -> momentId,
                        momentId -> {
                            List<Notification> findNotifications = notificationsByMomentIds.getOrDefault(momentId,
                                    List.of());
                            return findNotifications.stream()
                                    .map(Notification::getId)
                                    .toList();
                        }));
    }
}
