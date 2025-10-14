package moment.notification.domain;

import moment.user.domain.User;

public record PushNotificationCommand(User user, PushNotificationMessage message) {
}
