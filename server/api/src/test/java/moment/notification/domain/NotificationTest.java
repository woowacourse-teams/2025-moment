package moment.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import moment.config.TestTags;
import moment.fixture.UserFixture;
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
        SourceData sourceData = SourceData.of(Map.of("momentId", 42L));
        Notification notification = new Notification(
                user, NotificationType.NEW_COMMENT_ON_MOMENT, sourceData, "/moments/42");

        // when
        notification.markAsRead();

        // then
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void sourceData와_link를_포함하여_알림_객체를_생성한다() {
        // given
        User user = UserFixture.createUser();
        SourceData sourceData = SourceData.of(Map.of("groupId", 3L));
        String link = "/groups/3";

        // when
        Notification notification = new Notification(
                user, NotificationType.GROUP_JOIN_REQUEST, sourceData, link);

        // then
        assertThat(notification.getSourceData()).isEqualTo(sourceData);
        assertThat(notification.getLink()).isEqualTo(link);
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.GROUP_JOIN_REQUEST);
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    void GROUP_KICKED_알림은_link가_null이다() {
        // given
        User user = UserFixture.createUser();
        SourceData sourceData = SourceData.of(Map.of("groupId", 3L));

        // when
        Notification notification = new Notification(
                user, NotificationType.GROUP_KICKED, sourceData, null);

        // then
        assertThat(notification.getLink()).isNull();
    }
}
