package moment.comment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.fixture.UserFixture;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
public class CommentImageRepositoryTest {

    @Autowired
    CommentImageRepository commentImageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    CommentRepository commentRepository;

    @Test
    void 코멘트로_코멘트_이미지를_조회한다() {
        // given
        User momenter = UserFixture.createUser();
        User commenter1 = UserFixture.createUser();
        User commenter2 = UserFixture.createUser();
        User commenter3 = UserFixture.createUser();

        User savedMomenter = userRepository.save(momenter);
        User savedCommenter1 = userRepository.save(commenter1);
        User savedCommenter2 = userRepository.save(commenter2);
        User savedCommenter3 = userRepository.save(commenter3);

        Moment moment = new Moment("내용", savedMomenter, WriteType.BASIC);
        Moment savedMoment = momentRepository.save(moment);

        String imageUrl = "https://s3:moment-dev/images/고양이.jpg";
        String imageName = "고양이.jpg";

        Comment comment1 = new Comment("내용1", savedCommenter1, savedMoment.getId());
        Comment comment2 = new Comment("내용2", savedCommenter2, savedMoment.getId());
        Comment comment3 = new Comment("내용3", savedCommenter3, savedMoment.getId());
        Comment savedComment1 = commentRepository.save(comment1);
        Comment savedComment2 = commentRepository.save(comment2);
        Comment savedComment3 = commentRepository.save(comment3);

        List<Comment> comments = List.of(savedComment1, savedComment2, savedComment3);

        CommentImage commentImage1 = new CommentImage(savedComment1, imageUrl, imageName);
        CommentImage commentImage3 = new CommentImage(savedComment3, imageUrl, imageName);
        CommentImage savedCommentImage1 = commentImageRepository.save(commentImage1);
        CommentImage savedCommentImage3 = commentImageRepository.save(commentImage3);

        List<CommentImage> expected = List.of(savedCommentImage1, savedCommentImage3);

        // when
        List<CommentImage> results = commentImageRepository.findAllByCommentIn(comments);

        // then
        assertThat(results).isEqualTo(expected);
    }

    @Test
    void 코멘트로_코멘트_이미지를_삭제한다() {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment content", momenter, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("comment content", commenter, moment.getId()));

        String imageUrl = "https://example.com/image.jpg";
        String imageName = "image.jpg";
        CommentImage commentImage = new CommentImage(comment, imageUrl, imageName);
        commentImageRepository.save(commentImage);

        assertThat(commentImageRepository.count()).isEqualTo(1);

        // when
        commentImageRepository.deleteByComment(comment);

        // then
        assertThat(commentImageRepository.count()).isZero();
    }

    @Test
    void 코멘트로_코멘트_이미지가_있을_때_조회한다() {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment content", momenter, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("comment content", commenter, moment.getId()));

        String imageUrl = "https://example.com/image.jpg";
        String imageName = "image.jpg";
        CommentImage savedCommentImage = commentImageRepository.save(new CommentImage(comment, imageUrl, imageName));

        // when
        Optional<CommentImage> result = commentImageRepository.findByComment(comment);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(savedCommentImage);
    }

    @Test
    void 코멘트로_코멘트_이미지가_없을_때_빈_Optional을_반환한다() {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment content", momenter, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("comment content", commenter, moment.getId()));

        // when
        Optional<CommentImage> result = commentImageRepository.findByComment(comment);

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    void 코멘트_ID로_코멘트_이미지를_삭제한다() {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment content", momenter, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("comment content", commenter, moment.getId()));
        Long commentId = comment.getId();

        String imageUrl = "https://example.com/image.jpg";
        String imageName = "image.jpg";
        commentImageRepository.save(new CommentImage(comment, imageUrl, imageName));

        assertThat(commentImageRepository.count()).isEqualTo(1);

        // when
        commentImageRepository.deleteByCommentId(commentId);

        // then
        assertThat(commentImageRepository.count()).isZero();
    }
}
