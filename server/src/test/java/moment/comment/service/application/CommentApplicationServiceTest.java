package moment.comment.service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.dto.tobe.CommentCompositions;
import moment.block.domain.UserBlock;
import moment.block.infrastructure.UserBlockRepository;
import moment.comment.dto.response.GroupCommentResponse;
import moment.comment.infrastructure.CommentImageRepository;
import moment.comment.infrastructure.CommentRepository;
import moment.config.TestTags;
import moment.fixture.CommentFixture;
import moment.fixture.GroupFixture;
import moment.fixture.MomentFixture;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.support.CommentCreatedAtHelper;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentApplicationServiceTest {

    @Autowired
    private CommentApplicationService commentApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentImageRepository commentImageRepository;

    @Autowired
    private CommentCreatedAtHelper createdAtHelper;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserBlockRepository userBlockRepository;

    private User user;
    private Moment moment;

    @BeforeEach
    void setUp() {
        user = userRepository.save(UserFixture.createUser());
        moment = momentRepository.save(new Moment("moment content", user));
    }

    @Test
    void 이미지와_함께_코멘트를_생성한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("new comment", moment.getId(), "imageUrl", "imageName");

        // when
        CommentCreateResponse response = commentApplicationService.createComment(request, user.getId());
        Comment comment = commentRepository.findById(response.commentId()).get();

        // then
        assertAll(
                () -> assertThat(response.commentId()).isNotNull(),
                () -> assertThat(response.content()).isEqualTo("new comment"),
                () -> assertThat(commentRepository.findById(response.commentId())).isPresent(),
                () -> assertThat(commentImageRepository.findByComment(comment)).isPresent()
        );
    }

    @Test
    void 이미지가_없이_코멘트를_생성한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("new comment", moment.getId(), null, null);

        // when
        CommentCreateResponse response = commentApplicationService.createComment(request, user.getId());
        Comment comment = commentRepository.findById(response.commentId()).get();

        // then
        assertAll(
                () -> assertThat(response.commentId()).isNotNull(),
                () -> assertThat(response.content()).isEqualTo("new comment"),
                () -> assertThat(commentRepository.findById(response.commentId())).isPresent(),
                () -> assertThat(commentImageRepository.findByComment(comment)).isEmpty()
        );
    }

    @Test
    void 신고_횟수가_임계점을_넘으면_코멘트와_관련_데이터를_삭제한다() {
        // given
        Comment comment = commentRepository.save(new Comment("comment", user, moment.getId()));
        commentImageRepository.save(new CommentImage(comment, "url", "name"));

        long reportCount = 1;

        // when
        commentApplicationService.deleteByReport(comment.getId(), reportCount);

        // then
        assertThat(commentRepository.findById(comment.getId())).isEmpty();
        assertThat(commentImageRepository.findByComment(comment)).isEmpty();
    }

    @Test
    void 신고_횟수가_임계점_미만이면_코멘트를_삭제하지_않는다() {
        // given
        Comment comment = commentRepository.save(new Comment("comment", user, moment.getId()));
        commentImageRepository.save(new CommentImage(comment, "url", "name"));

        long reportCount = 0;

        // when
        commentApplicationService.deleteByReport(comment.getId(), reportCount);

        // then
        assertThat(commentRepository.findById(comment.getId())).isPresent();
        assertThat(commentImageRepository.findByComment(comment)).isPresent();
    }

    @Test
    void 코멘트_생성_유효성_검증에_성공한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("new comment", moment.getId(), null, null);

        // when & then
        commentApplicationService.validateCreateComment(request, user.getId());
    }

    @Test
    void 이미_코멘트를_작성했으면_유효성_검증에_실패한다() {
        // given
        commentRepository.save(new Comment("existing comment", user, moment.getId()));
        CommentCreateRequest request = new CommentCreateRequest("new comment", moment.getId(), null, null);

        // when & then
        assertThatThrownBy(() -> commentApplicationService.validateCreateComment(request, user.getId()))
                .isInstanceOf(MomentException.class)
                .hasMessageContaining(ErrorCode.COMMENT_CONFLICT.getMessage());
    }

    @Test
    void 내가_코멘트하지_않은_모멘트_ID_목록을_조회한다() {
        // given
        User otherUser = UserFixture.createUser();
        userRepository.save(otherUser);
        Moment moment2 = momentRepository.save(new Moment("moment 2", user));
        commentRepository.save(new Comment("my comment", user, moment.getId()));
        commentRepository.save(new Comment("other's comment", otherUser, moment2.getId()));

        List<Long> momentIds = List.of(moment.getId(), moment2.getId());

        // when
        List<Long> result = commentApplicationService.getMomentIdsNotCommentedByMe(momentIds, user.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(moment2.getId());
    }

    @Test
    void 모멘트_ID_목록으로_코멘트_구성_요소를_조회한다() {
        // given
        Comment comment = commentRepository.save(new Comment("comment", user, moment.getId()));
        String originalImageUrl = "https://cdn.moment.com/test/images/comment_photo.jpg";
        commentImageRepository.save(new CommentImage(comment, originalImageUrl, "name1"));

        Comment comment2 = commentRepository.save(new Comment("comment2", user, moment.getId()));

        // when
        List<CommentComposition> result = commentApplicationService.getMyCommentCompositionsBy(List.of(moment.getId()));
        CommentComposition composition1 = result.stream().filter(c -> c.id().equals(comment.getId()))
                .findFirst().orElseThrow();
        CommentComposition composition2 = result.stream().filter(c -> c.id().equals(comment2.getId()))
                .findFirst().orElseThrow();

        // then
        String expectedResolvedUrl = "https://cdn.moment.com/test/optimized-images/comment_photo.jpg";
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(composition1.content()).isEqualTo(comment.getContent()),
                () -> assertThat(composition1.nickname()).isEqualTo(user.getNickname()),
                () -> assertThat(composition1.imageUrl()).isEqualTo(expectedResolvedUrl),
                () -> assertThat(composition2.content()).isEqualTo(comment2.getContent()),
                () -> assertThat(composition2.nickname()).isEqualTo(user.getNickname()),
                () -> assertThat(composition2.imageUrl()).isNull()
        );
    }

    @Test
    void 나의_코멘트_구성_요소를_조회한다_첫_페이지() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        IntStream.range(0, 5).forEach(i -> {
            createdAtHelper.saveCommentWithCreatedAt("comment " + i, user, moment.getId(), start.plusHours(i));
        });

        // when
        CommentCompositions result = commentApplicationService.getMyCommentCompositions(
                new Cursor(null),
                new PageSize(3),
                user.getId());

        // then
        assertThat(result.hasNextPage()).isTrue();
        assertThat(result.commentCompositions()).hasSize(3);
        assertThat(result.commentCompositions().get(0).content()).isEqualTo("comment 4");
        assertThat(result.commentCompositions().get(1).content()).isEqualTo("comment 3");
        assertThat(result.commentCompositions().get(2).content()).isEqualTo("comment 2");
    }

    @Test
    void 나의_코멘트_구성_요소를_조회한다_두_번째_페이지() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        IntStream.range(0, 5).forEach(i -> {
            createdAtHelper.saveCommentWithCreatedAt("comment " + i, user, moment.getId(), start.plusHours(i));
        });

        Comment cursorComment = commentRepository.findAll().stream()
                .filter(c -> c.getContent().equals("comment 2"))
                .findFirst().orElseThrow();
        String cursorStr = cursorComment.getCreatedAt().toString() + "_" + cursorComment.getId();

        // when
        CommentCompositions result = commentApplicationService.getMyCommentCompositions(new Cursor(cursorStr),
                new PageSize(3), user.getId());

        // then
        assertThat(result.hasNextPage()).isFalse();
        assertThat(result.commentCompositions()).hasSize(2);
        assertThat(result.commentCompositions().get(0).content()).isEqualTo("comment 1");
        assertThat(result.commentCompositions().get(1).content()).isEqualTo("comment 0");
    }

    @Test
    void 그룹_댓글_조회_시_차단된_사용자의_댓글이_필터링된다() {
        // given
        User blockedUser = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroup(user));
        GroupMember member = groupMemberRepository.save(GroupMember.createOwner(group, user, "닉네임"));
        GroupMember blockedMember = groupMemberRepository.save(GroupMember.createOwner(group, blockedUser, "차단유저"));

        Moment groupMoment = momentRepository.save(MomentFixture.createMomentInGroup(user, group, member));
        commentRepository.save(CommentFixture.createCommentInGroup(groupMoment, user, member));
        commentRepository.save(CommentFixture.createCommentInGroup(groupMoment, blockedUser, blockedMember));

        userBlockRepository.save(new UserBlock(user, blockedUser));

        // when
        List<GroupCommentResponse> result = commentApplicationService.getCommentsInGroup(
                group.getId(), groupMoment.getId(), user.getId());

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void 차단된_사용자의_댓글에_좋아요_토글_시_예외가_발생한다() {
        // given
        User blockedUser = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroup(user));
        GroupMember member = groupMemberRepository.save(GroupMember.createOwner(group, user, "닉네임"));
        GroupMember blockedMember = groupMemberRepository.save(GroupMember.createOwner(group, blockedUser, "차단유저"));

        Moment groupMoment = momentRepository.save(MomentFixture.createMomentInGroup(user, group, member));
        Comment blockedComment = commentRepository.save(
                CommentFixture.createCommentInGroup(groupMoment, blockedUser, blockedMember));

        userBlockRepository.save(new UserBlock(user, blockedUser));

        // when & then
        assertThatThrownBy(() -> commentApplicationService.toggleCommentLike(
                group.getId(), blockedComment.getId(), user.getId()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BLOCKED_USER_INTERACTION);
    }
}
