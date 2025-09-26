package moment.notification.application;

import moment.user.domain.User;

public record PushNotificationCommand(User user, String title, String body) {
}
