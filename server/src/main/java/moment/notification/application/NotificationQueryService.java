package moment.notification.application;

import java.util.List;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.user.domain.User;

public interface NotificationQueryService {

    Notification getNotificationById(Long id);

    List<Notification> getNotificationsByIds(List<Long> ids);

    List<Notification> getUnreadContentsNotifications(User user, TargetType targetType);
}
