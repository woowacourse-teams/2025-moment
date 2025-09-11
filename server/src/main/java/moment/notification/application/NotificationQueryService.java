package moment.notification.application;

import moment.notification.domain.Notification;

public interface NotificationQueryService {

    Notification getNotificationById(Long id);
}
