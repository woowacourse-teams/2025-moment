package moment.notification.application;

import moment.notification.dto.PushNotificationRequest;

public interface PushNotificationService {

    void send(PushNotificationRequest pushNotificationRequest);
}
