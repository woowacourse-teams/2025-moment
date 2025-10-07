package moment.notification.service.tobe;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
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

    public List<Notification> getUnreadNotifications(User user, TargetType targetType) {
        boolean isRead = false;
        return notificationRepository.findAllByUserAndIsReadAndTargetType(user, isRead, targetType);
    }

    public List<Long> getUnreadTargetIdsBy(Long userId, TargetType targetType) {
        boolean isRead = false;
        return notificationRepository.findAllByUserIdAndIsReadAndTargetType(userId, isRead, targetType);
    }

    public Notification saveNotification(
            User user, 
            Long targetId, 
            NotificationType notificationType,
            TargetType targetType
    ) {
        Notification notification = new Notification(user, notificationType, targetType, targetId);
        return notificationRepository.save(notification);
    }
}
