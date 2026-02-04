package moment.notification.service.notification;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.infrastructure.NotificationRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Long> getUnreadTargetIdsBy(Long userId, TargetType targetType) {
        boolean isRead = false;
        return notificationRepository.findAllByUserIdAndIsReadAndTargetType(userId, isRead, targetType);
    }

    @Transactional
    public Notification save(User user, Long targetId, NotificationType notificationType,
                             TargetType targetType, Long groupId) {
        Notification notification = (groupId != null)
                ? new Notification(user, notificationType, targetType, targetId, groupId)
                : new Notification(user, notificationType, targetType, targetId);
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsBy(List<Long> targetIds, boolean isRead, TargetType targetType) {
        return notificationRepository.findNotificationsBy(targetIds, isRead, targetType);
    }

    public List<Notification> getAllBy(Long userId, boolean read) {
        return notificationRepository.findAllByUserIdAndIsRead(userId, read);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new MomentException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(List<Long> notificationIds) {
        List<Notification> notifications = notificationRepository.findAllById(notificationIds);

        notifications.forEach(Notification::markAsRead);
    }
}
