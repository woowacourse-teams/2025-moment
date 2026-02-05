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

@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
public class NotificationTest {

    @Test
    void 알림_객체를_읽으면_참이_된다() {
        // given
        User user = UserFixture.createUser();
        NotificationType notificationType = NotificationType.NEW_COMMENT_ON_MOMENT;
        TargetType targetType = TargetType.MOMENT;

        Notification notification = new Notification(user, notificationType, targetType, 1L);
        // when

        notification.markAsRead();

        // then
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void groupId를_포함하여_알림_객체를_생성한다() {
        // given
        User user = UserFixture.createUser();
        NotificationType notificationType = NotificationType.GROUP_JOIN_REQUEST;
        TargetType targetType = TargetType.GROUP;
        Long targetId = 1L;
        Long groupId = 100L;

        // when
        Notification notification = new Notification(user, notificationType, targetType, targetId, groupId);

        // then
        assertThat(notification.getGroupId()).isEqualTo(groupId);
        assertThat(notification.getTargetId()).isEqualTo(targetId);
        assertThat(notification.getNotificationType()).isEqualTo(notificationType);
        assertThat(notification.getTargetType()).isEqualTo(targetType);
        assertThat(notification.isRead()).isFalse();
    }
}
