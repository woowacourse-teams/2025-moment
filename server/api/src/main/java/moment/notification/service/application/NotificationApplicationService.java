package moment.notification.service.application;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.SourceData;
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

    private static final List<NotificationType> MOMENT_TYPES =
            List.of(NotificationType.NEW_COMMENT_ON_MOMENT, NotificationType.MOMENT_LIKED);

    private static final List<NotificationType> COMMENT_TYPES =
            List.of(NotificationType.COMMENT_LIKED);

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
    public Notification createNotification(Long userId, NotificationType notificationType,
                                            SourceData sourceData, String link) {
        User user = userService.getUserBy(userId);
        return notificationService.save(user, notificationType, sourceData, link);
    }

    public List<Long> getUnreadMomentIds(Long userId) {
        List<Notification> notifications = notificationService.getAllBy(
                userId, false, MOMENT_TYPES);
        return notifications.stream()
                .map(n -> n.getSourceData().getLong("momentId"))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public List<Long> getUnreadCommentIds(Long userId) {
        List<Notification> notifications = notificationService.getAllBy(
                userId, false, COMMENT_TYPES);
        return notifications.stream()
                .map(n -> n.getSourceData().getLong("commentId"))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public Map<Long, List<Long>> getNotificationsByMomentIds(Long userId, List<Long> momentIds) {
        List<Notification> notifications = notificationService.getAllBy(userId, false, MOMENT_TYPES);
        return groupNotificationIdsBySourceId(notifications, "momentId", momentIds);
    }

    public Map<Long, List<Long>> getNotificationsByCommentIds(Long userId, List<Long> commentIds) {
        List<Notification> notifications = notificationService.getAllBy(userId, false, COMMENT_TYPES);
        return groupNotificationIdsBySourceId(notifications, "commentId", commentIds);
    }

    private Map<Long, List<Long>> groupNotificationIdsBySourceId(
            List<Notification> notifications, String sourceKey, List<Long> targetIds) {
        Map<Long, List<Notification>> grouped = notifications.stream()
                .filter(n -> n.getSourceData().getLong(sourceKey) != null)
                .collect(Collectors.groupingBy(n -> n.getSourceData().getLong(sourceKey)));

        return targetIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> grouped.getOrDefault(id, List.of()).stream()
                                .map(Notification::getId)
                                .toList()));
    }
}
