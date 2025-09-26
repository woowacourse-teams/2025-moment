package moment.notification.dto;

import moment.user.domain.User;

public record PushNotificationRequest(User user, String title, String body) {

    public static PushNotificationRequest createAddedCommentAlarm(User momenter) {
        return new PushNotificationRequest(
                momenter,
                "[moment]",
                "당신의 모멘트에 누군가 코멘트를 달았어요:)");
    }
}
