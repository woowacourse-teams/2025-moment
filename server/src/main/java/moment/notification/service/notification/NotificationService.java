package moment.notification.service.notification;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.SourceData;
import moment.notification.infrastructure.NotificationRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification save(User user, NotificationType notificationType,
                             SourceData sourceData, String link) {
        return notificationRepository.save(
                new Notification(user, notificationType, sourceData, link));
    }

    public List<Notification> getAllBy(Long userId, boolean isRead, List<NotificationType> types) {
        return notificationRepository.findAllByUserIdAndIsReadAndNotificationTypeIn(
                userId, isRead, types);
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
