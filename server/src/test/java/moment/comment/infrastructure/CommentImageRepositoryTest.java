package moment.comment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.ProviderType;
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
        User momenter = new User("emial@meail.com", "1234!", "마아", ProviderType.EMAIL);
        User commenter1 = new User("errr@meail.com", "1234!", "라고", ProviderType.EMAIL);
        User commenter2 = new User("err@meail.com", "1234!", "포히", ProviderType.EMAIL);
        User commenter3 = new User("er@meail.com", "1234!", "미미", ProviderType.EMAIL);

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
}
