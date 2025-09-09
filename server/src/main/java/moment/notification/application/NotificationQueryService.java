package moment.notification.application;

import java.util.List;
import moment.notification.domain.Notification;
import moment.user.domain.User;

public interface NotificationQueryService {

    Notification getNotificationById(Long id);

    List<Notification> getUnreadMomentNotifications(User user);
}
