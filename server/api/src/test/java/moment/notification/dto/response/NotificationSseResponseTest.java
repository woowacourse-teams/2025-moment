package moment.notification.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.SourceData;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationSseResponseTest {

    @Test
    void link가_포함된_Notification에서_NotificationSseResponse를_생성한다() {
        // given
        User user = UserFixture.createUser();
        SourceData sourceData = SourceData.of(Map.of("momentId", 1L));
        Notification notification = new Notification(
                user, NotificationType.NEW_COMMENT_ON_MOMENT, sourceData, "/moments/1");

        // when
        NotificationSseResponse response = NotificationSseResponse.from(notification);

        // then
        assertThat(response.link()).isEqualTo("/moments/1");
        assertThat(response.notificationType()).isEqualTo(NotificationType.NEW_COMMENT_ON_MOMENT);
        assertThat(response.message()).isEqualTo(NotificationType.NEW_COMMENT_ON_MOMENT.getMessage());
    }

    @Test
    void link가_null인_Notification에서_NotificationSseResponse를_생성한다() {
        // given
        User user = UserFixture.createUser();
        SourceData sourceData = SourceData.of(Map.of("groupId", 3L));
        Notification notification = new Notification(
                user, NotificationType.GROUP_KICKED, sourceData, null);

        // when
        NotificationSseResponse response = NotificationSseResponse.from(notification);

        // then
        assertThat(response.link()).isNull();
    }
}
