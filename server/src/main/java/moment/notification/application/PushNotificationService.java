package moment.notification.application;

import moment.notification.domain.PushNotificationCommand;

public interface PushNotificationService {

    void send(PushNotificationCommand pushNotificationCommand);
}
