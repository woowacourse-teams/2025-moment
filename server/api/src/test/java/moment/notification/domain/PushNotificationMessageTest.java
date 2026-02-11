package moment.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import moment.config.TestTags;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class PushNotificationMessageTest {

    @Test
    void 모든_NotificationType에_대해_PushNotificationMessage가_매핑된다() {
        for (NotificationType type : NotificationType.values()) {
            PushNotificationMessage message = PushNotificationMessage.from(type);
            assertThat(message).isNotNull();
        }
    }

    @Test
    void NEW_COMMENT_ON_MOMENT은_REPLY_TO_MOMENT으로_매핑된다() {
        assertThat(PushNotificationMessage.from(NotificationType.NEW_COMMENT_ON_MOMENT))
                .isEqualTo(PushNotificationMessage.REPLY_TO_MOMENT);
    }

    @Test
    void GROUP_JOIN_REQUEST는_GROUP_JOIN_REQUEST로_매핑된다() {
        assertThat(PushNotificationMessage.from(NotificationType.GROUP_JOIN_REQUEST))
                .isEqualTo(PushNotificationMessage.GROUP_JOIN_REQUEST);
    }

    @Test
    void MOMENT_LIKED는_MOMENT_LIKED로_매핑된다() {
        assertThat(PushNotificationMessage.from(NotificationType.MOMENT_LIKED))
                .isEqualTo(PushNotificationMessage.MOMENT_LIKED);
    }
}
