package moment.notification.domain;

import lombok.Getter;

@Getter
public enum NotificationType {
    NEW_COMMENT_ON_MOMENT("내 모멘트에 새로운 코멘트가 달렸습니다."),

    // 그룹 관련 타입
    GROUP_JOIN_REQUEST("그룹 가입 신청이 있습니다."),
    GROUP_JOIN_APPROVED("그룹 가입이 승인되었습니다."),
    GROUP_KICKED("그룹에서 강퇴되었습니다."),

    // 좋아요 관련 신규 타입
    MOMENT_LIKED("모멘트에 좋아요가 달렸습니다."),
    COMMENT_LIKED("코멘트에 좋아요가 달렸습니다.");

    private final String message;

    NotificationType(String message) {
        this.message = message;
    }
}
