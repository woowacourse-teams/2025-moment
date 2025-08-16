package moment.reward.domain;

import lombok.Getter;

@Getter
public enum Reason {
    COMMENT_CREATION(5),
    POSITIVE_EMOJI_RECEIVED(15),

    CANCEL_POSITIVE_EMOJI_RECEIVED(-15),
    MOMENT_ADDITIONAL_USE(-10),
    ;

    private final int pointTo;

    Reason(int pointTo) {
        this.pointTo = pointTo;
    }
}
