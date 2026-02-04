package moment.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.domain.TargetType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationPayloadTest {

    @Test
    void MOMENT_타겟으로_생성_시_moments_링크를_생성한다() {
        // given
        Notification notification = createNotification(
                NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT, 42L, null);

        // when
        NotificationPayload payload = NotificationPayload.from(notification);

        // then
        assertThat(payload.link()).isEqualTo("/moments/42");
    }

    @Test
    void GROUP_타겟으로_생성_시_groups_링크를_생성한다() {
        // given
        Notification notification = createNotification(
                NotificationType.GROUP_JOIN_REQUEST, TargetType.GROUP, 10L, 100L);

        // when
        NotificationPayload payload = NotificationPayload.from(notification);

        // then
        assertThat(payload.link()).isEqualTo("/groups/100");
    }

    @Test
    void COMMENT_타겟으로_생성_시_comments_링크를_생성한다() {
        // given
        Notification notification = createNotification(
                NotificationType.COMMENT_LIKED, TargetType.COMMENT, 7L, null);

        // when
        NotificationPayload payload = NotificationPayload.from(notification);

        // then
        assertThat(payload.link()).isEqualTo("/comments/7");
    }

    @Test
    void NotificationType_메시지를_포함한다() {
        // given
        Notification notification = createNotification(
                NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT, 1L, null);

        // when
        NotificationPayload payload = NotificationPayload.from(notification);

        // then
        assertThat(payload.message()).isEqualTo(NotificationType.NEW_COMMENT_ON_MOMENT.getMessage());
    }

    private Notification createNotification(NotificationType type, TargetType targetType,
                                             Long targetId, Long groupId) {
        User user = UserFixture.createUser();
        Notification notification = (groupId != null)
                ? new Notification(user, type, targetType, targetId, groupId)
                : new Notification(user, type, targetType, targetId);
        ReflectionTestUtils.setField(notification, "id", 1L);
        return notification;
    }
}
