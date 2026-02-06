package moment.notification.infrastructure;

import java.util.List;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserIdAndIsRead(Long userId, boolean isRead);

    List<Notification> findAllByUserIdAndIsReadAndNotificationTypeIn(
            Long userId, boolean isRead, List<NotificationType> notificationTypes);

    List<Notification> findAllByIsReadAndNotificationTypeIn(
            boolean isRead, List<NotificationType> notificationTypes);
}
