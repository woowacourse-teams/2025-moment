package moment.notification.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.infrastructure.NotificationRepository;
import moment.notification.service.notification.SseNotificationService;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationFacadeServiceTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationFacadeService notificationFacadeService;

    @MockitoBean
    private SseNotificationService sseNotificationService;

    @Test
    void 알림을_생성하고_SSE_이벤트를_전송한다() {
        // given
        boolean unreadFlag = false;
        User user = userRepository.save(UserFixture.createUser());
        Long userId = user.getId();
        Long contentId = 10L;
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;
        TargetType contentType = TargetType.MOMENT;

        // when
        notificationFacadeService.createNotificationAndSendSse(userId, contentId, reason, contentType);

        // then
        List<Notification> notifications = notificationRepository.findAllByUserIdAndIsRead(userId, unreadFlag);
        assertAll(
                () -> assertThat(notifications).hasSize(1),
                () -> verify(sseNotificationService).sendToClient(eq(userId), eq("notification"), any())
        );
    }
}