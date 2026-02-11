package moment.notification.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationType 테스트")
class NotificationTypeTest {

    @Test
    @DisplayName("그룹 관련 알림 타입이 존재한다")
    void NotificationType_그룹_관련_타입_존재() {
        assertThat(NotificationType.values()).contains(
            NotificationType.GROUP_JOIN_REQUEST,
            NotificationType.GROUP_JOIN_APPROVED,
            NotificationType.GROUP_KICKED
        );
    }

    @Test
    @DisplayName("좋아요 관련 알림 타입이 존재한다")
    void NotificationType_좋아요_관련_타입_존재() {
        assertThat(NotificationType.values()).contains(
            NotificationType.MOMENT_LIKED,
            NotificationType.COMMENT_LIKED
        );
    }

    @Test
    @DisplayName("새 코멘트 알림 타입이 존재한다")
    void NotificationType_새_코멘트_타입_존재() {
        assertThat(NotificationType.values()).contains(
            NotificationType.NEW_COMMENT_ON_MOMENT
        );
    }
}
