package moment.notification.application;

public interface PushNotificationService {

    void send(PushNotificationCommand pushNotificationCommand);
}
