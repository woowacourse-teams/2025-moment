package moment.comment.service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Set;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.comment.domain.Echo;
import moment.comment.dto.CommentForEcho;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.dto.tobe.CommentCompositions;
import moment.comment.dto.tobe.EchoDetail;
import moment.comment.infrastructure.CommentImageRepository;
import moment.comment.infrastructure.CommentRepository;
import moment.comment.infrastructure.EchoRepository;
import moment.comment.service.comment.CommentImageService;
import moment.comment.service.comment.CommentService;
import moment.comment.service.comment.EchoService;
import moment.common.DatabaseCleaner;
import moment.global.config.AppConfig;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import moment.user.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import({DatabaseCleaner.class, AppConfig.class, CommentApplicationService.class, CommentService.class,
        CommentImageService.class, EchoService.class, UserService.class})
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentApplicationServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

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
    private EchoRepository echoRepository;

    private User user;
    private Moment moment;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        user = userRepository.save(new User("test@email.com", "password", "tester", ProviderType.EMAIL));
        moment = momentRepository.save(new Moment("moment content", user, WriteType.BASIC));
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
        assertThat(response.commentId()).isNotNull();
        assertThat(response.content()).isEqualTo("new comment");
        assertThat(commentRepository.findById(response.commentId())).isPresent();
        assertThat(commentImageRepository.findByComment(comment)).isEmpty();
    }

    @Test
    void 신고_횟수가_임계점을_넘으면_코멘트와_관련_데이터를_삭제한다() {
        // given
        Comment comment = commentRepository.save(new Comment("comment", user, moment.getId()));
        commentImageRepository.save(new CommentImage(comment, "url", "name"));
        echoRepository.save(new Echo("HAPPY", user, comment));

        long reportCount = 1;

        // when
        commentApplicationService.deleteByReport(comment.getId(), reportCount);

        // then
        assertThat(commentRepository.findById(comment.getId())).isEmpty();
        assertThat(commentImageRepository.findByComment(comment)).isEmpty();
        assertThat(echoRepository.findByComment(comment)).isEmpty();
    }

    @Test
    void 신고_횟수가_임계점_미만이면_코멘트를_삭제하지_않는다() {
        // given
        Comment comment = commentRepository.save(new Comment("comment", user, moment.getId()));
        commentImageRepository.save(new CommentImage(comment, "url", "name"));
        echoRepository.save(new Echo("HAPPY", user, comment));

        long reportCount = 0;

        // when
        commentApplicationService.deleteByReport(comment.getId(), reportCount);

        // then
        assertThat(commentRepository.findById(comment.getId())).isPresent();
        assertThat(commentImageRepository.findByComment(comment)).isPresent();
        assertThat(echoRepository.findByComment(comment)).isNotEmpty();
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
        User otherUser = userRepository.save(new User("other@email.com", "pw", "other", ProviderType.EMAIL));
        Moment moment2 = momentRepository.save(new Moment("moment 2", user, WriteType.BASIC));
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
    void 에코를_위한_코멘트_정보를_조회한다() {
        // given
        Comment savedComment = commentRepository.save(new Comment("comment", user, moment.getId()));

        // when
        CommentForEcho result = commentApplicationService.getCommentForEchoBy(savedComment.getId());

        // then
        assertThat(result.commenterId()).isEqualTo(savedComment.getCommenter().getId());
        assertThat(result.momentId()).isEqualTo(savedComment.getMomentId());
    }

    @Test
    void 존재하지_않는_코멘트_ID로_에코용_정보를_조회하면_예외가_발생한다() {
        // given
        Long nonExistentCommentId = 999L;

        // when & then
        assertThatThrownBy(() -> commentApplicationService.getCommentForEchoBy(nonExistentCommentId))
                .isInstanceOf(MomentException.class)
                .hasMessageContaining(ErrorCode.COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    void 코멘트에_에코를_생성한다() {
        // given
        Comment savedComment = commentRepository.save(new Comment("comment", user, moment.getId()));
        Set<String> echoTypes = Set.of("HAPPY", "SAD");

        // when
        commentApplicationService.createEcho(savedComment.getId(), user.getId(), echoTypes);

        // then
        List<Echo> echos = echoRepository.findAllByComment(savedComment);
        assertThat(echos).hasSize(2);

        List<String> actualEchoTypes = echos.stream().map(Echo::getEchoType).toList();
        assertThat(actualEchoTypes).containsExactlyInAnyOrder("HAPPY", "SAD");
    }

    @Test
    void 코멘트의_에코_목록을_조회한다() {
        // given
        Comment savedComment = commentRepository.save(new Comment("comment", user, moment.getId()));
        echoRepository.save(new Echo("HAPPY", user, savedComment));
        echoRepository.save(new Echo("SAD", user, savedComment));

        // when
        List<EchoDetail> echoDetails = commentApplicationService.getEchosBy(savedComment.getId());

        // then
        assertThat(echoDetails).hasSize(2);
        assertThat(echoDetails.get(0).echoType()).isEqualTo("HAPPY");
        assertThat(echoDetails.get(1).echoType()).isEqualTo("SAD");
    }

    @Test
    void 모멘트_ID_목록으로_코멘트_구성_요소를_조회한다() {
        // given
        Comment comment = commentRepository.save(new Comment("comment", user, moment.getId()));
        commentImageRepository.save(new CommentImage(comment, "url1", "name1"));
        echoRepository.save(new Echo("HAPPY", user, comment));

        Comment comment2 = commentRepository.save(new Comment("comment2", user, moment.getId()));

        // when
        List<CommentComposition> result = commentApplicationService.getMyCommentCompositionsBy(List.of(moment.getId()));
        CommentComposition composition1 = result.stream().filter(c -> c.id().equals(comment.getId()))
                .findFirst().orElseThrow();
        CommentComposition composition2 = result.stream().filter(c -> c.id().equals(comment2.getId()))
                .findFirst().orElseThrow();

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(composition1.content()).isEqualTo(comment.getContent()),
                () -> assertThat(composition1.nickname()).isEqualTo(user.getNickname()),
                () -> assertThat(composition1.imageUrl()).isEqualTo("url1"),
                () -> assertThat(composition1.echoDetails()).hasSize(1),
                () -> assertThat(composition1.echoDetails().get(0).echoType()).isEqualTo("HAPPY"),
                () -> assertThat(composition2.content()).isEqualTo(comment2.getContent()),
                () -> assertThat(composition2.nickname()).isEqualTo(user.getNickname()),
                () -> assertThat(composition2.imageUrl()).isNull(),
                () -> assertThat(composition2.echoDetails()).isEmpty()
        );
    }

    @Test
    void 나의_코멘트_구성_요소를_조회한다_첫_페이지() {
        // given
        for (int i = 0; i < 5; i++) {
            commentRepository.save(new Comment("comment " + i, user, moment.getId()));
        }
        commentRepository.flush();

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
        for (int i = 0; i < 5; i++) {
            commentRepository.save(new Comment("comment " + i, user, moment.getId()));
        }
        commentRepository.flush();

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
}
