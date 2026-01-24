package moment.moment.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.List;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.config.TestTags;
import moment.fixture.CommentFixture;
import moment.fixture.GroupFixture;
import moment.fixture.GroupMemberFixture;
import moment.fixture.MomentFixture;
import moment.fixture.UserFixture;
import moment.global.domain.TargetType;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
import moment.moment.domain.Moment;
import moment.moment.dto.response.MyGroupFeedResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.infrastructure.NotificationRepository;
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
@ActiveProfiles("test")
@Transactional
@DisplayNameGeneration(ReplaceUnderscores.class)
class MyGroupMomentPageFacadeServiceTest {

    @Autowired
    private MyGroupMomentPageFacadeService facadeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User user;
    private Group group;
    private GroupMember member;

    @BeforeEach
    void setUp() {
        user = userRepository.save(UserFixture.createUser());
        group = groupRepository.save(GroupFixture.createGroup(user));
        member = groupMemberRepository.save(GroupMember.createOwner(group, user, "닉네임"));
    }

    @Test
    void 그룹_내_나의_모멘트를_조회한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMomentInGroup(user, group, member));
        Comment comment = CommentFixture.createCommentInGroup(moment, user, member);
        commentRepository.save(comment);

        // when
        MyGroupFeedResponse response = facadeService.getMyMomentsInGroup(
                group.getId(), user.getId(), null);

        // then
        assertAll(
                () -> assertThat(response.moments()).hasSize(1),
                () -> assertThat(response.moments().get(0).comments()).hasSize(1),
                () -> assertThat(response.moments().get(0).momentNotification()).isNotNull()
        );
    }

    @Test
    void 그룹_내_모멘트가_없으면_빈_응답을_반환한다() {
        // given
        // 모멘트 없음

        // when
        MyGroupFeedResponse response = facadeService.getMyMomentsInGroup(
                group.getId(), user.getId(), null);

        // then
        assertAll(
                () -> assertThat(response.moments()).isEmpty(),
                () -> assertThat(response.hasNextPage()).isFalse()
        );
    }

    @Test
    void 읽지_않은_알림이_있는_모멘트를_조회한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMomentInGroup(user, group, member));
        Notification notification = new Notification(
                user, NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT, moment.getId(), group.getId());
        notificationRepository.save(notification);

        // when
        MyGroupFeedResponse response = facadeService.getUnreadMyMomentsInGroup(
                group.getId(), user.getId(), null);

        // then
        assertAll(
                () -> assertThat(response.moments()).hasSize(1),
                () -> assertThat(response.moments().get(0).momentNotification().isRead()).isFalse()
        );
    }

    @Test
    void 읽지_않은_알림이_없으면_빈_응답을_반환한다() {
        // given
        momentRepository.save(MomentFixture.createMomentInGroup(user, group, member));
        // 알림 없음

        // when
        MyGroupFeedResponse response = facadeService.getUnreadMyMomentsInGroup(
                group.getId(), user.getId(), null);

        // then
        assertThat(response.moments()).isEmpty();
    }

    @Test
    void 커서_기반_페이지네이션이_동작한다() {
        // given
        List<Moment> moments = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            moments.add(momentRepository.save(MomentFixture.createMomentInGroup(user, group, member)));
        }

        // when - 첫 페이지
        MyGroupFeedResponse firstPage = facadeService.getMyMomentsInGroup(
                group.getId(), user.getId(), null);

        // then
        assertAll(
                () -> assertThat(firstPage.moments()).hasSize(10),
                () -> assertThat(firstPage.hasNextPage()).isTrue(),
                () -> assertThat(firstPage.nextCursor()).isNotNull()
        );

        // when - 두 번째 페이지
        MyGroupFeedResponse secondPage = facadeService.getMyMomentsInGroup(
                group.getId(), user.getId(), firstPage.nextCursor());

        // then
        assertAll(
                () -> assertThat(secondPage.moments()).hasSize(5),
                () -> assertThat(secondPage.hasNextPage()).isFalse()
        );
    }
}
