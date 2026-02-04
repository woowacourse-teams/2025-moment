package moment.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PushNotificationMessage {
    REPLY_TO_MOMENT("Moment", "당신의 모멘트에 누군가 코멘트를 달았어요:)"),

    // 그룹 관련 푸시 메시지
    GROUP_JOIN_REQUEST("Moment", "누군가 그룹 가입을 신청했어요"),
    GROUP_JOIN_APPROVED("Moment", "그룹 가입이 승인되었어요"),
    GROUP_KICKED("Moment", "그룹에서 강퇴되었어요"),

    // 좋아요 관련 푸시 메시지
    MOMENT_LIKED("Moment", "누군가 당신의 모멘트를 좋아해요"),
    COMMENT_LIKED("Moment", "누군가 당신의 코멘트를 좋아해요"),
    ;

    private final String title;
    private final String body;
}
