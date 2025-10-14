package moment.notification.domain;

public interface PushNotificationSender {

    void send(PushNotificationCommand pushNotificationCommand);
}
