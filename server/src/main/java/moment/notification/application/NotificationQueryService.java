package moment.notification.application;

import java.util.List;
import moment.notification.domain.Notification;
import moment.notification.domain.TargetType;
import moment.user.domain.User;

public interface NotificationQueryService {

    Notification getNotificationById(Long id);

    List<Notification> getUnreadContentsNotifications(User user, TargetType targetType);
}
