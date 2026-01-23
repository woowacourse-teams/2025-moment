package moment.comment.service.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.support.CommentCreatedAtHelper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserRepository userRepository;

    private CommentService commentService;
    @Autowired
    private CommentCreatedAtHelper commentCreatedAtHelper;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        commentService = new CommentService(commentRepository);
    }

    @AfterEach
    void down() {
        databaseCleaner.clean();
    }

    @Test
    void 모멘트_ID_목록으로_모든_코멘트를_조회한다() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());

        Moment moment1 = momentRepository.save(new Moment("moment 1", momentOwner));
        Moment moment2 = momentRepository.save(new Moment("moment 2", momentOwner));
        Moment moment3 = momentRepository.save(new Moment("moment 3", momentOwner)); // Not in search

        Comment comment1 = commentRepository.save(new Comment("comment on moment 1", commenter, moment1.getId()));
        Comment comment2 = commentRepository.save(new Comment("comment on moment 2", commenter, moment2.getId()));
        commentRepository.save(new Comment("comment on moment 3", commenter, moment3.getId()));

        // when
        List<Comment> result = commentService.getAllByMomentIds(List.of(moment1.getId(), moment2.getId()));

        // then
        assertThat(result).hasSize(2)
                .containsExactlyInAnyOrder(comment1, comment2);
    }

    @Test
    void 모멘트_중_내가_코멘트하지_않은_모멘트ID_목록을_조회한다() {
        // given
        User me = userRepository.save(UserFixture.createUser());
        User other = userRepository.save(UserFixture.createUser());
        User momentOwner = userRepository.save(UserFixture.createUser());

        Moment moment1 = momentRepository.save(new Moment("Moment 1", momentOwner)); // me만 코멘트
        Moment moment2 = momentRepository.save(new Moment("Moment 2", momentOwner)); // other만 코멘트
        Moment moment3 = momentRepository.save(
                new Moment("Moment 3", momentOwner)); // me와 other 모두 코멘트

        commentRepository.save(new Comment("My comment", me, moment1.getId()));
        commentRepository.save(new Comment("Other's comment", other, moment2.getId()));
        commentRepository.save(new Comment("My comment on 3", me, moment3.getId()));
        commentRepository.save(new Comment("Other's comment on 3", other, moment3.getId()));

        List<Long> momentIdsToSearch = List.of(moment1.getId(), moment2.getId(), moment3.getId());

        // when
        List<Long> result = commentService.getMomentIdsNotCommentedByMe(momentIdsToSearch, me.getId());

        // then
        // me를 제외한 다른 사람이 코멘트한 모멘트 ID 목록을 반환해야 합니다. (moment2)
        assertThat(result).hasSize(1)
                .containsExactlyInAnyOrder(moment2.getId());
    }

    @Test
    void 코멘트_ID로_코멘트를_삭제한다() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner));
        Comment comment = commentRepository.save(new Comment("comment", commenter, moment.getId()));

        assertThat(commentRepository.findById(comment.getId())).isPresent();

        // when
        commentService.deleteBy(comment.getId());

        // then
        assertThat(commentRepository.findById(comment.getId())).isNotPresent();
    }

    @Test
    void 새로운_코멘트를_생성한다() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner));
        Comment newComment = new Comment("new comment", commenter, moment.getId());

        // when
        Comment savedComment = commentService.create(newComment);

        // then
        assertThat(savedComment.getId()).isNotNull();
        assertThat(commentRepository.findById(savedComment.getId())).isPresent();
    }

    @Test
    void 모멘트에_이미_코멘트를_남겼으면_예외가_발생한다() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner));
        commentRepository.save(new Comment("existing comment", commenter, moment.getId()));

        // when & then
        assertThatThrownBy(() -> commentService.validateUniqueBy(moment.getId(), commenter.getId()))
                .isInstanceOf(MomentException.class)
                .hasMessageContaining(ErrorCode.COMMENT_CONFLICT.getMessage());
    }

    @Test
    void 모멘트에_코멘트를_남기지_않았으면_예외가_발생하지_않는다() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner));

        // when & then
        assertThatCode(() -> commentService.validateUniqueBy(moment.getId(), commenter.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void 작성자별로_코멘트_목록을_조회한다_첫_페이지() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner));

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        IntStream.range(0, 5).forEach(i -> {
            commentCreatedAtHelper.saveCommentWithCreatedAt("comment " + i, commenter, moment.getId(),
                    start.plusHours(i));
        });

        PageSize pageSize = new PageSize(3);
        Cursor cursor = new Cursor(null);

        // when
        List<Comment> result = commentService.getCommentsBy(commenter, cursor, pageSize);

        // then
        assertAll(
                () -> assertThat(result).hasSize(4), // pageSize + 1
                () -> assertThat(result.get(0).getId()).isEqualTo(5L),
                () -> assertThat(result.get(1).getId()).isEqualTo(4L),
                () -> assertThat(result.get(2).getId()).isEqualTo(3L),
                () -> assertThat(result.get(3).getId()).isEqualTo(2L)
        );
    }

    @Test
    void 작성자별로_코멘트_목록을_조회한다_두_번째_페이지() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner));

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        IntStream.range(0, 5).forEach(i -> {
            commentCreatedAtHelper.saveCommentWithCreatedAt("comment " + i, commenter, moment.getId(),
                    start.plusHours(i));
        });

        List<Comment> allComments = commentRepository.findAll().stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed()
                        .thenComparing(Comment::getId).reversed())
                .toList();

        Comment lastCommentOfFirstPage = allComments.get(2);
        String cursorStr = lastCommentOfFirstPage.getCreatedAt().toString() + "_" + lastCommentOfFirstPage.getId();

        PageSize pageSize = new PageSize(3);
        Cursor cursor = new Cursor(cursorStr);

        // when
        List<Comment> result = commentService.getCommentsBy(commenter, cursor, pageSize);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).getId()).isEqualTo(2L),
                () -> assertThat(result.get(1).getId()).isEqualTo(1L)
        );
    }

    @Test
    void 코멘트_ID_목록으로_코멘트를_조회한다_첫_페이지() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner));

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        IntStream.range(0, 5).forEach(i -> {
            commentCreatedAtHelper.saveCommentWithCreatedAt("comment " + i, commenter, moment.getId(),
                    start.plusHours(i));
        });

        List<Long> commentIds = commentRepository.findAll().stream().map(Comment::getId).toList();
        PageSize pageSize = new PageSize(3);
        Cursor cursor = new Cursor(null);

        // when
        List<Comment> result = commentService.getCommentsBy(commentIds, cursor, pageSize);

        // then
        assertAll(
                () -> assertThat(result).hasSize(4),
                () -> assertThat(result.get(0).getId()).isEqualTo(5L),
                () -> assertThat(result.get(1).getId()).isEqualTo(4L),
                () -> assertThat(result.get(2).getId()).isEqualTo(3L),
                () -> assertThat(result.get(3).getId()).isEqualTo(2L)
        );
    }

    @Test
    void 코멘트_ID_목록으로_코멘트를_조회한다_두_번째_페이지() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner));

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        IntStream.range(0, 5).forEach(i -> {
            commentCreatedAtHelper.saveCommentWithCreatedAt("comment " + i, commenter, moment.getId(),
                    start.plusHours(i));
        });

        List<Long> commentIds = commentRepository.findAll().stream().map(Comment::getId).toList();
        Comment cursorComment = commentRepository.findById(3L).orElseThrow();
        String cursorStr = cursorComment.getCreatedAt().toString() + "_" + cursorComment.getId();

        PageSize pageSize = new PageSize(3);
        Cursor cursor = new Cursor(cursorStr);

        // when
        List<Comment> result = commentService.getCommentsBy(commentIds, cursor, pageSize);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).getId()).isEqualTo(2L),
                () -> assertThat(result.get(1).getId()).isEqualTo(1L)
        );
    }

    @Test
    void 코멘트_ID로_모멘트_ID를_조회한다() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner));
        Comment comment = commentRepository.save(new Comment("A comment", commenter, moment.getId()));

        // when
        Long foundMomentId = commentService.getMomentIdBy(comment.getId());

        // then
        assertThat(foundMomentId).isEqualTo(moment.getId());
    }

    @Test
    void 존재하지_않는_코멘트_ID로_모멘트_ID를_조회하면_예외가_발생한다() {
        // given
        Long nonExistentCommentId = 999L;

        // when & then
        assertThatThrownBy(() -> commentService.getMomentIdBy(nonExistentCommentId))
                .isInstanceOf(MomentException.class)
                .hasMessageContaining(ErrorCode.COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    void 코멘트_ID로_코멘트를_조회한다() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner));
        Comment comment = commentRepository.save(new Comment("A comment", commenter, moment.getId()));

        // when
        Comment foundComment = commentService.getCommentBy(comment.getId());

        // then
        assertThat(foundComment).isEqualTo(comment);
    }

    @Test
    void 존재하지_않는_코멘트_ID로_코멘트를_조회하면_예외가_발생한다() {
        // given
        Long nonExistentCommentId = 999L;

        // when & then
        assertThatThrownBy(() -> commentService.getCommentBy(nonExistentCommentId))
                .isInstanceOf(MomentException.class)
                .hasMessageContaining(ErrorCode.COMMENT_NOT_FOUND.getMessage());
    }
}
