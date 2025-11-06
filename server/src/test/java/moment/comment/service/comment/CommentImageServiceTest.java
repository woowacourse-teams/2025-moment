package moment.comment.service.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.comment.infrastructure.CommentImageRepository;
import moment.comment.infrastructure.CommentRepository;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
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
class CommentImageServiceTest {

    @Autowired
    private CommentImageRepository commentImageRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserRepository userRepository;

    private CommentImageService commentImageService;

    private User user;
    private Moment moment;
    private Comment comment;

    @BeforeEach
    void setUp() {
        commentImageService = new CommentImageService(commentImageRepository);

        user = userRepository.save(UserFixture.createUser());
        moment = momentRepository.save(new Moment("moment content", user, WriteType.BASIC));
        comment = commentRepository.save(new Comment("comment content", user, moment.getId()));
    }

    @Test
    void 이미지_정보가_있으면_코멘트_이미지를_생성한다() {
        // given
        String imageUrl = "imageUrl.jpg";

        // when
        Optional<CommentImage> resultOpt = commentImageService.create(comment, imageUrl, "imageName");

        // then
        assertAll(
                () -> assertThat(resultOpt).isPresent(),
                () -> assertThat(resultOpt.get().getId()).isNotNull(),
                () -> assertThat(resultOpt.get().getComment()).isEqualTo(comment),
                () -> assertThat(resultOpt.get().getImageUrl()).isEqualTo(imageUrl),
                () -> assertThat(resultOpt.get().getImageName()).isEqualTo("imageName"),
                () -> assertThat(commentImageRepository.findById(resultOpt.get().getId())).isPresent()
        );
    }

    @Test
    void 이미지_정보가_없으면_코멘트_이미지를_생성하지_않는다() {
        // given & when
        Optional<CommentImage> resultOpt = commentImageService.create(comment, null, null);

        // then
        assertThat(resultOpt).isEmpty();
    }

    @Test
    void 코멘트_목록으로_코멘트_이미지_맵을_조회한다() {
        // given
        Comment comment2 = commentRepository.save(new Comment("comment content 2", user, moment.getId()));
        CommentImage commentImage1 = commentImageRepository.save(new CommentImage(comment, "url1", "name1"));
        CommentImage commentImage2 = commentImageRepository.save(new CommentImage(comment2, "url2", "name2"));

        // when
        Map<Comment, CommentImage> result = commentImageService.getCommentImageByComment(List.of(comment, comment2));

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(comment)).isEqualTo(commentImage1),
                () -> assertThat(result.get(comment2)).isEqualTo(commentImage2)
        );
    }

    @Test
    void 코멘트_객체로_코멘트_이미지를_삭제한다() {
        // given
        CommentImage commentImage = commentImageRepository.save(new CommentImage(comment, "url", "name"));
        assertThat(commentImageRepository.findById(commentImage.getId())).isPresent();

        // when
        commentImageService.deleteByComment(comment);

        // then
        assertThat(commentImageRepository.findById(commentImage.getId())).isEmpty();
    }

    @Test
    void 코멘트_ID로_코멘트_이미지를_삭제한다() {
        // given
        CommentImage commentImage = commentImageRepository.save(new CommentImage(comment, "url", "name"));
        assertThat(commentImageRepository.findById(commentImage.getId())).isPresent();

        // when
        commentImageService.deleteBy(comment.getId());

        // then
        assertThat(commentImageRepository.findById(commentImage.getId())).isEmpty();
    }
}
