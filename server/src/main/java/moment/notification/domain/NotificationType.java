package moment.notification.domain;

import lombok.Getter;

@Getter
public enum NotificationType {
    NEW_COMMENT_ON_MOMENT("내 모멘트에 새로운 코멘트가 달렸습니다."),
    NEW_REPLY_ON_COMMENT("내 코멘트에 새로운 답장이 달렸습니다.");

    private final String message;

    NotificationType(String message) {
        this.message = message;
    }
}
