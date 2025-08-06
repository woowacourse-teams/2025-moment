package moment.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
public class NotificationTest {

    @Test
    void 알림_객체를_읽으면_참이_된다() {
        // given
        User user = new User("curry@stephan.com", "curry1234!", "3점의신", ProviderType.EMAIL);
        NotificationType notificationType = NotificationType.NEW_COMMENT_ON_MOMENT;
        TargetType targetType = TargetType.MOMENT;

        Notification notification = new Notification(user, notificationType, targetType, 1L);
        // when

        notification.checkNotification();

        // then
        assertThat(notification.isRead()).isTrue();
    }
}
