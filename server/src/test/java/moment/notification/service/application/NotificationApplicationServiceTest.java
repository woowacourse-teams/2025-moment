package moment.notification.service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Map;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.SourceData;
import moment.notification.dto.request.NotificationReadRequest;
import moment.notification.dto.response.NotificationResponse;
import moment.notification.infrastructure.NotificationRepository;
import moment.notification.service.notification.SseNotificationService;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    @AfterEach
    void down() {
        databaseCleaner.clean();
    }

    @Test
    void 사용자의_알림_목록을_조회한다() {
        // given
        notificationRepository.save(new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 1L)), "/moments/1"));
        notificationRepository.save(new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 2L)), "/moments/2"));

        Notification readNotification = new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 3L)), "/moments/3");
        readNotification.markAsRead();
        notificationRepository.save(readNotification);

        notificationRepository.save(new Notification(anotherUser,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 1L)), "/moments/1"));

        // when
        List<NotificationResponse> responses = notificationApplicationService.getNotificationBy(
                user.getId(), false);

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    void 알림을_읽음_처리한다() {
        // given
        Notification notification = notificationRepository.save(
                new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT,
                        SourceData.of(Map.of("momentId", 1L)), "/moments/1"));

        // when
        notificationApplicationService.markAsRead(notification.getId());

        // then
        Notification result = notificationRepository.findById(notification.getId()).get();
        assertThat(result.isRead()).isTrue();
    }

    @Test
    void 여러_알림을_한번에_읽음_처리한다() {
        // given
        Notification notification1 = notificationRepository.save(
                new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT,
                        SourceData.of(Map.of("momentId", 1L)), "/moments/1"));
        Notification notification2 = notificationRepository.save(
                new Notification(user, NotificationType.NEW_COMMENT_ON_MOMENT,
                        SourceData.of(Map.of("momentId", 2L)), "/moments/2"));

        List<Long> notificationIds = List.of(notification1.getId(), notification2.getId());
        NotificationReadRequest request = new NotificationReadRequest(notificationIds);

        // when
        notificationApplicationService.markAllAsRead(request);

        // then
        List<Notification> results = notificationRepository.findAllById(notificationIds);
        assertThat(results).allMatch(Notification::isRead);
    }

    @Test
    void 알림을_생성한다() {
        // given
        Long userId = user.getId();
        NotificationType notificationType = NotificationType.NEW_COMMENT_ON_MOMENT;
        SourceData sourceData = SourceData.of(Map.of("momentId", 10L));
        String link = "/moments/10";

        // when
        notificationApplicationService.createNotification(userId, notificationType, sourceData, link);
        List<Notification> notifications = notificationRepository.findAllByUserIdAndIsRead(userId, false);

        // then
        assertThat(notifications).hasSize(1);
    }

    @Test
    void 읽지_않은_모멘트_ID_목록을_조회한다() {
        // given
        notificationRepository.save(new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 10L)), "/moments/10"));
        notificationRepository.save(new Notification(user,
                NotificationType.MOMENT_LIKED,
                SourceData.of(Map.of("momentId", 20L)), "/moments/20"));
        notificationRepository.save(new Notification(user,
                NotificationType.COMMENT_LIKED,
                SourceData.of(Map.of("commentId", 30L)), "/comments/30"));

        // when
        List<Long> result = notificationApplicationService.getUnreadMomentIds(user.getId());

        // then
        assertThat(result).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void 읽지_않은_코멘트_ID_목록을_조회한다() {
        // given
        notificationRepository.save(new Notification(user,
                NotificationType.COMMENT_LIKED,
                SourceData.of(Map.of("commentId", 15L)), "/comments/15"));
        notificationRepository.save(new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 10L)), "/moments/10"));

        // when
        List<Long> result = notificationApplicationService.getUnreadCommentIds(user.getId());

        // then
        assertThat(result).containsExactly(15L);
    }

    @Test
    void 모멘트_ID로_읽지_않은_알림을_조회하고_맵으로_반환한다() {
        // given
        List<Long> momentIds = List.of(100L, 200L, 300L);

        notificationRepository.save(new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 100L)), "/moments/100"));

        notificationRepository.save(new Notification(user,
                NotificationType.COMMENT_LIKED,
                SourceData.of(Map.of("commentId", 100L)), "/comments/100"));

        notificationRepository.save(new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 400L)), "/moments/400"));

        Notification readNotification = new Notification(user,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 200L)), "/moments/200");
        readNotification.markAsRead();
        notificationRepository.save(readNotification);

        // when
        Map<Long, List<Long>> result = notificationApplicationService.getNotificationsByMomentIds(momentIds);

        // then
        assertAll(
                () -> assertThat(result.get(100L)).hasSize(1),
                () -> assertThat(result.get(200L)).hasSize(0),
                () -> assertThat(result.get(300L)).hasSize(0)
        );
    }
}
