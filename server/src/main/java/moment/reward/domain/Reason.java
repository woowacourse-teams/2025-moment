package moment.reward.domain;

import lombok.Getter;

@Getter
public enum Reason {
    COMMENT_CREATION(5),
    POSITIVE_EMOJI_RECEIVED(15),

    CANCEL_POSITIVE_EMOJI_RECEIVED(-15),
    // 임시 작성, 나중에 리워드 작업물과 통합될 때 실제 메서드 시그니처들이 들어올 예정
    MOMENT_CREATION(5),
    ;

    private final int pointTo;

    Reason(int pointTo) {
        this.pointTo = pointTo;
    }
}
