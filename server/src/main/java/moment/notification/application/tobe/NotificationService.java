package moment.notification.application.tobe;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
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
}
