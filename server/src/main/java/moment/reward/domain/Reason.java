package moment.reward.domain;

import lombok.Getter;

@Getter
public enum Reason {
    MOMENT_CREATION(5),
    COMMENT_CREATION(2),
    ECHO_RECEIVED(3),
    ;

    private final int pointTo;

    Reason(int pointTo) {
        this.pointTo = pointTo;
    }
}
