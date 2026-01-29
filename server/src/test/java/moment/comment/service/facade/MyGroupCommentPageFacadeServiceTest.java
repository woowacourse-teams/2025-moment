package moment.comment.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.List;
import moment.comment.domain.Comment;
import moment.comment.dto.response.MyGroupCommentListResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.config.TestTags;
import moment.fixture.CommentFixture;
import moment.fixture.GroupFixture;
import moment.fixture.MomentFixture;
import moment.fixture.UserFixture;
import moment.global.domain.TargetType;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
import moment.moment.domain.Moment;
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
class MyGroupCommentPageFacadeServiceTest {

    @Autowired
    private MyGroupCommentPageFacadeService facadeService;

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
    void 그룹_내_나의_코멘트를_조회한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMomentInGroup(user, group, member));
        Comment comment = CommentFixture.createCommentInGroup(moment, user, member);
        commentRepository.save(comment);

        // when
        MyGroupCommentListResponse response = facadeService.getMyCommentsInGroup(
                group.getId(), user.getId(), null);

        // then
        assertAll(
                () -> assertThat(response.comments()).hasSize(1),
                () -> assertThat(response.comments().get(0).moment()).isNotNull(),
                () -> assertThat(response.comments().get(0).commentNotification()).isNotNull(),
                () -> assertThat(response.comments().get(0).likeCount()).isEqualTo(0L),
                () -> assertThat(response.comments().get(0).hasLiked()).isFalse(),
                () -> assertThat(response.comments().get(0).moment().likeCount()).isEqualTo(0L),
                () -> assertThat(response.comments().get(0).moment().hasLiked()).isFalse()
        );
    }

    @Test
    void 그룹_내_코멘트가_없으면_빈_응답을_반환한다() {
        // given
        // 코멘트 없음

        // when
        MyGroupCommentListResponse response = facadeService.getMyCommentsInGroup(
                group.getId(), user.getId(), null);

        // then
        assertAll(
                () -> assertThat(response.comments()).isEmpty(),
                () -> assertThat(response.hasNextPage()).isFalse()
        );
    }

    @Test
    void 읽지_않은_알림이_있는_코멘트를_조회한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMomentInGroup(user, group, member));
        Comment comment = commentRepository.save(CommentFixture.createCommentInGroup(moment, user, member));
        Notification notification = new Notification(
                user, NotificationType.COMMENT_LIKED, TargetType.COMMENT, comment.getId(), group.getId());
        notificationRepository.save(notification);

        // when
        MyGroupCommentListResponse response = facadeService.getUnreadMyCommentsInGroup(
                group.getId(), user.getId(), null);

        // then
        assertAll(
                () -> assertThat(response.comments()).hasSize(1),
                () -> assertThat(response.comments().get(0).commentNotification().isRead()).isFalse(),
                () -> assertThat(response.comments().get(0).likeCount()).isEqualTo(0L),
                () -> assertThat(response.comments().get(0).hasLiked()).isFalse(),
                () -> assertThat(response.comments().get(0).moment().likeCount()).isEqualTo(0L),
                () -> assertThat(response.comments().get(0).moment().hasLiked()).isFalse()
        );
    }

    @Test
    void 읽지_않은_알림이_없으면_빈_응답을_반환한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMomentInGroup(user, group, member));
        commentRepository.save(CommentFixture.createCommentInGroup(moment, user, member));
        // 알림 없음

        // when
        MyGroupCommentListResponse response = facadeService.getUnreadMyCommentsInGroup(
                group.getId(), user.getId(), null);

        // then
        assertThat(response.comments()).isEmpty();
    }

    @Test
    void 커서_기반_페이지네이션이_동작한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMomentInGroup(user, group, member));
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            comments.add(commentRepository.save(CommentFixture.createCommentInGroup(moment, user, member)));
        }

        // when - 첫 페이지
        MyGroupCommentListResponse firstPage = facadeService.getMyCommentsInGroup(
                group.getId(), user.getId(), null);

        // then
        assertAll(
                () -> assertThat(firstPage.comments()).hasSize(10),
                () -> assertThat(firstPage.hasNextPage()).isTrue(),
                () -> assertThat(firstPage.nextCursor()).isNotNull()
        );

        // when - 두 번째 페이지
        MyGroupCommentListResponse secondPage = facadeService.getMyCommentsInGroup(
                group.getId(), user.getId(), firstPage.nextCursor());

        // then
        assertAll(
                () -> assertThat(secondPage.comments()).hasSize(5),
                () -> assertThat(secondPage.hasNextPage()).isFalse()
        );
    }
}
