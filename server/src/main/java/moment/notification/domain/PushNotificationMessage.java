package moment.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PushNotificationMessage {
    REPLY_TO_MOMENT("[moment]", "당신의 모멘트에 누군가 코멘트를 달았어요:)"),
    ;

    private final String title;
    private final String body;
}
