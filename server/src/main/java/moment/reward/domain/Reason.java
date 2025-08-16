package moment.reward.domain;

import lombok.Getter;

@Getter
public enum Reason {
    MOMENT_CREATION(5),
    COMMENT_CREATION(2),
    ECHO_RECEIVED(3),
    MOMENT_ADDITIONAL_USE(-4),
    NICKNAME_CHANGE(-100);

    private final int pointTo;

    Reason(int pointTo) {
        this.pointTo = pointTo;
    }
}
