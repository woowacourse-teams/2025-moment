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
    void Comment_ID와_일치하는_Comment_목록을_생성_시간_내림차순으로_조회한다() {
        // given
        User momenter1 = new User("kiki@icloud.com", "1234", "kiki");
        userRepository.save(momenter1);

        User momenter2 = new User("ama@gmail.com", "1234", "ama");
        userRepository.save(momenter2);

        User commenter = new User("hippo@gmail.com", "1234", "hippo");
        User savedCommenter = userRepository.save(commenter);

        Moment moment1 = new Moment("오늘 하루는 행복한 하루~", true, momenter1);
        Moment savedMoment1 = momentRepository.save(moment1);

        Moment moment2 = new Moment("오늘 하루는 맛있는 하루~", true, momenter2);
        Moment savedMoment2 = momentRepository.save(moment2);

        Comment comment1 = new Comment("moment1 comment", commenter, moment1);
        Comment savedComment1 = commentRepository.save(comment1);

        Comment comment2 = new Comment("moment2 comment", commenter, moment2);
        Comment savedComment2 = commentRepository.save(comment2);

        // when
        List<Comment> comments = commentRepository.findCommentsByCommenterIdOrderByCreatedAtDesc(savedCommenter.getId());

        // then
        assertAll(
                () -> assertThat(comments).hasSize(2),
                () -> assertThat(comments.getFirst()).isEqualTo(savedComment2),
                () -> assertThat(comments.getLast()).isEqualTo(savedComment1),
                () -> assertThat(comments.getFirst().getMoment()).isEqualTo(savedMoment2),
                () -> assertThat(comments.getLast().getMoment()).isEqualTo(savedMoment1)
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
