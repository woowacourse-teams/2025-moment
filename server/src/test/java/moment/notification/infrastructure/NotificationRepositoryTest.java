package moment.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationRepositoryTest {

    private final boolean readFlag = true;
    private final boolean unReadFlag = false;
    private final long contentId = 1L;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    private User user;
    private User anotherUser;
    private long userId;

    @BeforeEach
    void setUp() {
        user = UserFixture.createUser();
        userRepository.save(user);
        userId = user.getId();

        anotherUser = UserFixture.createUser();
        userRepository.save(anotherUser);
    }

    @Test
    void user_id를_고려하여_읽지_않은_알림들을_조회한다() {
        // given
        TargetType contentType1 = TargetType.MOMENT;
        NotificationType reason1 = NotificationType.NEW_COMMENT_ON_MOMENT;

        TargetType contentType2 = TargetType.COMMENT;
        NotificationType reason2 = NotificationType.NEW_REPLY_ON_COMMENT;

        Notification expectedNotification = new Notification(user, reason1, contentType1, contentId);

        Notification readNotification = new Notification(user, reason2, contentType2, contentId);
        readNotification.checkNotification();

        Notification anotherUserNotification = new Notification(anotherUser, reason1, contentType1, contentId);

        notificationRepository.saveAll(
                List.of(expectedNotification, readNotification, anotherUserNotification)
        );

        // when
        List<Notification> unreadNotifications = notificationRepository.findAllByUserIdAndIsRead(userId, unReadFlag);

        // then
        assertAll(
                () -> assertThat(unreadNotifications).hasSize(1),
                () -> assertThat(unreadNotifications.stream()
                        .noneMatch(Notification::isRead))
                        .isTrue()
        );
    }

    @Test
    void user_id를_고려하여_읽은_알림들을_조회한다() {
        // given
        TargetType contentType1 = TargetType.MOMENT;
        NotificationType reason1 = NotificationType.NEW_COMMENT_ON_MOMENT;

        TargetType contentType2 = TargetType.COMMENT;
        NotificationType reason2 = NotificationType.NEW_REPLY_ON_COMMENT;

        Notification unreadNotification = new Notification(user, reason1, contentType1, contentId);

        Notification readNotification = new Notification(user, reason2, contentType2, contentId);
        readNotification.checkNotification();

        Notification anotherUserNotification = new Notification(anotherUser, reason2, contentType2, contentId);
        anotherUserNotification.checkNotification();

        notificationRepository.saveAll(
                List.of(unreadNotification, readNotification, anotherUserNotification)
        );

        // when
        List<Notification> readNotifications = notificationRepository.findAllByUserIdAndIsRead(userId, readFlag);

        // then
        assertAll(
                () -> assertThat(readNotifications).hasSize(1),
                () -> assertThat(readNotifications.stream()
                        .allMatch(Notification::isRead))
                        .isTrue()
        );
    }

    @Test
    void user_id와_타겟_타입을_고려하여_읽지_않은_알림의_컨텐츠_id들을_조회한다() {
        // given
        TargetType contentType = TargetType.MOMENT;
        TargetType anotherContentType = TargetType.COMMENT;

        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;
        NotificationType anotherReason = NotificationType.NEW_REPLY_ON_COMMENT;

        Notification expectedNotification = new Notification(user, reason, contentType, contentId);
        Notification anotherTypeNotification = new Notification(user, anotherReason, anotherContentType, contentId);
        Notification anotherUserNotification = new Notification(anotherUser, reason, contentType, contentId);

        notificationRepository.saveAll(
                List.of(expectedNotification, anotherTypeNotification, anotherUserNotification)
        );

        // when
        List<Long> expectedContentIds = notificationRepository.findAllByUserIdAndIsReadAndTargetType(
                userId, unReadFlag, contentType);

        // then
        assertAll(
                () -> assertThat(expectedContentIds).hasSize(1),
                () -> assertThat(expectedContentIds).contains(expectedNotification.getTargetId())
        );
    }

    @Test
    void user_id와_타겟_타입을_고려하여_읽은_알림의_컨텐츠_id들을_조회한다() {
        // given
        TargetType contentType = TargetType.MOMENT;
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;

        TargetType anotherContentType = TargetType.COMMENT;
        NotificationType anotherReason = NotificationType.NEW_REPLY_ON_COMMENT;

        Notification expectedNotification = new Notification(user, reason, contentType, contentId);
        expectedNotification.checkNotification();

        Notification anotherTypeNotification = new Notification(user, anotherReason, anotherContentType, contentId);
        Notification anotherUserNotification = new Notification(anotherUser, reason, contentType, contentId);

        notificationRepository.saveAll(
                List.of(expectedNotification, anotherTypeNotification, anotherUserNotification)
        );

        // when
        List<Long> expectedContentIds = notificationRepository.findAllByUserIdAndIsReadAndTargetType(
                userId, readFlag, contentType);

        // then
        assertAll(
                () -> assertThat(expectedContentIds).hasSize(1),
                () -> assertThat(expectedContentIds).contains(expectedNotification.getTargetId())
        );
    }

    @Test
    void 타켓_id와_타겟_타입으로_읽지_않은_알림_목록을_조회한다() {
        // given
        Long contentId1 = 1L;
        Long contentId2 = 2L;

        TargetType contentType = TargetType.MOMENT;
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;

        TargetType anotherContentType = TargetType.COMMENT;

        User user2 = UserFixture.createUser();
        userRepository.save(user2);

        Notification expectedNotification1 = new Notification(user, reason, contentType, contentId1);
        Notification expectedNotification2 = new Notification(user, reason, contentType, contentId2);
        Notification expectedNotification3 = new Notification(user2, reason, contentType, contentId1);

        Notification anotherTypeNotification = new Notification(user, reason, anotherContentType, contentId1);

        notificationRepository.saveAll(
                List.of(expectedNotification1,
                        expectedNotification2,
                        expectedNotification3,
                        anotherTypeNotification));

        List<Long> contentIds = List.of(contentId1, contentId2);

        // when
        List<Notification> foundNotifications = notificationRepository.findNotificationsBy(
                contentIds, unReadFlag, contentType);

        // then
        assertAll(
                () -> assertThat(foundNotifications).hasSize(3),
                () -> assertThat(foundNotifications).containsExactlyInAnyOrder(
                        expectedNotification1,
                        expectedNotification2,
                        expectedNotification3)
        );
    }

    @Test
    void 타켓_id와_타겟_타입으로_읽은_알림_목록을_조회한다() {
        // given
        Long contentId1 = 1L;
        Long contentId2 = 2L;

        TargetType contentType = TargetType.MOMENT;
        NotificationType reason = NotificationType.NEW_COMMENT_ON_MOMENT;

        TargetType anotherContentType = TargetType.COMMENT;

        User user2 = UserFixture.createUser();
        userRepository.save(user2);

        Notification expectedNotification1 = new Notification(user, reason, contentType, contentId1);
        expectedNotification1.checkNotification();

        Notification expectedNotification2 = new Notification(user, reason, contentType, contentId2);
        expectedNotification2.checkNotification();

        Notification expectedNotification3 = new Notification(user2, reason, contentType, contentId1);
        expectedNotification3.checkNotification();

        Notification anotherTypeNotification = new Notification(user, reason, anotherContentType, contentId1);

        notificationRepository.saveAll(
                List.of(expectedNotification1,
                        expectedNotification2,
                        expectedNotification3,
                        anotherTypeNotification));

        List<Long> contentIds = List.of(contentId1, contentId2);

        // when
        List<Notification> foundNotifications = notificationRepository.findNotificationsBy(
                contentIds, readFlag, contentType);

        // then
        assertAll(
                () -> assertThat(foundNotifications).hasSize(3),
                () -> assertThat(foundNotifications).containsExactlyInAnyOrder(
                        expectedNotification1,
                        expectedNotification2,
                        expectedNotification3)
        );
    }
}
