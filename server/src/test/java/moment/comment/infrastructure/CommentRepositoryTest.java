package moment.comment.infrastructure;

import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void Comment_ID와_일치하는_Comment_목록을_조회한다() {
        // given
        User momenter = new User("kiki@icloud.com", "1234", "kiki");
        userRepository.save(momenter);

        User commenter = new User("hippo@gmail.com", "1234", "hippo");
        User savedCommenter = userRepository.save(commenter);

        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter);
        Moment savedMoment = momentRepository.save(moment);

        Comment comment = new Comment("첫 번째 댓글", commenter, moment);
        Comment savedComment = commentRepository.save(comment);

        // when
        List<Comment> comments = commentRepository.findCommentsByCommenterId(savedCommenter.getId());

        // then
        assertAll(
                () -> assertThat(comments).hasSize(1),
                () -> assertThat(comments.getFirst()).isEqualTo(savedComment),
                () -> assertThat(comments.getFirst().getMoment()).isEqualTo(savedMoment)
        );
    }

    @Test
    void Momment의_Comment가_존재하면_true를_반환한다() {
        // given
        User momenter = new User("kiki@icloud.com", "1234", "kiki");
        userRepository.save(momenter);

        User commenter = new User("hippo@gmail.com", "1234", "hippo");
        userRepository.save(commenter);

        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter);
        momentRepository.save(moment);

        Comment comment = new Comment("첫 번째 댓글", commenter, moment);
        commentRepository.save(comment);

        // when & then
        assertThat(commentRepository.existsByMoment(moment)).isTrue();
    }

    @Test
    void Momment의_Comment가_존재하면_false를_반환한다() {
        // given
        User momenter = new User("kiki@icloud.com", "1234", "kiki");
        userRepository.save(momenter);

        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter);
        momentRepository.save(moment);

        // when & then
        assertThat(commentRepository.existsByMoment(moment)).isFalse();
    }
}
