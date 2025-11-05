package moment.notification.service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import moment.common.DatabaseCleaner;
import moment.fixture.UserFixture;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.dto.request.NotificationReadRequest;
import moment.notification.dto.response.NotificationResponse;
import moment.notification.infrastructure.NotificationRepository;
import moment.notification.service.notification.SseNotificationService;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationApplicationServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private NotificationApplicationService notificationApplicationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private SseNotificationService sseNotificationService;

    private User user;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        user = userRepository.save(UserFixture.createUser());
        anotherUser = userRepository.save(UserFixture.createUser());
    }

    @Test
    void 사용자의_알림_목록을_조회한다() {
        // given
        boolean unReadFlag = false;
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;
        TargetType contentType = TargetType.MOMENT;
        long contentId1 = 1L;
        long contentId2 = 2L;
        long readContentId = 3L;

        notificationRepository.save(new Notification(user, reason, contentType, contentId1));
        notificationRepository.save(new Notification(user, reason, contentType, contentId2));

        Notification readNotification = new Notification(user, reason, contentType, readContentId);
        readNotification.checkNotification();
        notificationRepository.save(readNotification);

        Notification anotherUserNotification = new Notification(anotherUser, reason, contentType, contentId1);
        notificationRepository.save(anotherUserNotification);

        // when
        List<NotificationResponse> responses = notificationApplicationService.getNotificationBy(user.getId(),
                unReadFlag);

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    void 알림을_읽음_처리한다() {
        // given
        Notification notification = notificationRepository.save(
                new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT, 1L));

        // when
        notificationApplicationService.markAsRead(notification.getId());

        // then
        Notification result = notificationRepository.findById(notification.getId()).get();
        assertThat(result.isRead()).isTrue();
    }

    @Test
    void 여러_알림을_한번에_읽음_처리한다() {
        // given
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;
        TargetType contentType = TargetType.MOMENT;
        long contentId1 = 1L;
        long contentId2 = 2L;

        Notification notification1 = notificationRepository.save(
                new Notification(user, reason, contentType, contentId1));
        Notification notification2 = notificationRepository.save(
                new Notification(user, reason, contentType, contentId2));

        List<Long> notificationIds = List.of(notification1.getId(), notification2.getId());
        NotificationReadRequest request = new NotificationReadRequest(notificationIds);

        // when
        notificationApplicationService.markAllAsRead(request);

        // then
        List<Notification> results = notificationRepository.findAllById(notificationIds);
        assertThat(results).allMatch(Notification::isRead);
    }

    @Test
    void 알림을_생성하고_SSE_이벤트를_전송한다() {
        // given
        boolean unreadFlag = false;
        Long userId = user.getId();
        Long contentId = 10L;
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;
        TargetType contentType = TargetType.MOMENT;

        // when
        notificationApplicationService.createNotificationAndSendSse(userId, contentId, reason, contentType);

        // then
        List<Notification> notifications = notificationRepository.findAllByUserIdAndIsRead(userId, unreadFlag);
        assertAll(
                () -> assertThat(notifications).hasSize(1),
                () -> verify(sseNotificationService).sendToClient(eq(userId), eq("notification"), any())
        );
    }

    @Test
    void 읽지_않은_알림의_타겟_ID_목록을_조회한다() {
        // given
        TargetType targetType = TargetType.MOMENT;
        notificationRepository.save(new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, targetType, 10L));
        notificationRepository.save(
                new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.COMMENT, 20L));
        Notification readNotification = new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT, targetType, 30L);
        readNotification.checkNotification();
        notificationRepository.save(readNotification);

        // when
        List<Long> result = notificationApplicationService.getUnreadNotifications(user.getId(), targetType);

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0)).isEqualTo(10L)
        );
    }

    @Test
    void 타겟_ID와_Type으로_읽지_않은_알림을_조회하고_맵으로_반환한다() {
        // given
        List<Long> contentIds = List.of(100L, 200L, 300L);
        TargetType contentType = TargetType.MOMENT;
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;

        TargetType anotherType = TargetType.COMMENT;
        NotificationType anotherReason = NotificationType.NEW_REPLY_ON_COMMENT;

        long contentId = 100L;
        long readContentId = 200L;
        long anotherContentId = 400L;

        Notification expectedNotification = new Notification(user, reason, contentType, contentId);
        notificationRepository.save(expectedNotification);

        Notification anotherTypeNotification = new Notification(user, anotherReason, anotherType, contentId);
        notificationRepository.save(anotherTypeNotification);

        Notification anotherTargetIdNotification = new Notification(user, reason, contentType, anotherContentId);
        notificationRepository.save(anotherTargetIdNotification);

        Notification readNotification = new Notification(user, reason, contentType, readContentId);
        readNotification.checkNotification();
        notificationRepository.save(readNotification);

        // when
        Map<Long, List<Long>> result = notificationApplicationService.getNotificationsByTargetIdsAndTargetType(
                contentIds, contentType);

        // then
        assertAll(
                () -> assertThat(result.get(100L)).hasSize(1),
                () -> assertThat(result.get(200L)).hasSize(0),
                () -> assertThat(result.get(300L)).hasSize(0)
        );
    }
}
