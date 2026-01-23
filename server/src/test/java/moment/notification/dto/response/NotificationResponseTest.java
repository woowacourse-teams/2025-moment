package moment.notification.dto.response;

import moment.fixture.UserFixture;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationResponse 테스트")
class NotificationResponseTest {

    @Test
    @DisplayName("groupId가 포함된 Notification에서 NotificationResponse를 생성한다")
    void groupId_포함_NotificationResponse_생성() {
        // Given
        User user = UserFixture.createUser();
        NotificationType notificationType = NotificationType.GROUP_JOIN_REQUEST;
        TargetType targetType = TargetType.GROUP;
        Long targetId = 1L;
        Long groupId = 100L;

        Notification notification = new Notification(user, notificationType, targetType, targetId, groupId);

        // When
        NotificationResponse response = NotificationResponse.from(notification);

        // Then
        assertThat(response.groupId()).isEqualTo(groupId);
        assertThat(response.targetId()).isEqualTo(targetId);
        assertThat(response.notificationType()).isEqualTo(notificationType);
        assertThat(response.targetType()).isEqualTo(targetType);
    }

    @Test
    @DisplayName("groupId가 null인 Notification에서 NotificationResponse를 생성한다")
    void groupId_null_NotificationResponse_생성() {
        // Given
        User user = UserFixture.createUser();
        NotificationType notificationType = NotificationType.MOMENT_LIKED;
        TargetType targetType = TargetType.MOMENT;
        Long targetId = 1L;

        Notification notification = new Notification(user, notificationType, targetType, targetId);

        // When
        NotificationResponse response = NotificationResponse.from(notification);

        // Then
        assertThat(response.groupId()).isNull();
        assertThat(response.targetId()).isEqualTo(targetId);
    }
}
