package moment.reply.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.reply.domain.Emoji;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class EmojiRepositoryTest {

    @Autowired
    EmojiRepository emojiRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    CommentRepository commentRepository;

    @Test
    void 코멘트에_달린_모든_이모지를_조회한다() {
        // given
        User momenter = userRepository.save(new User("ekorea623@gmail.com", "1q2w3e4r", "drago"));
        User commenter = userRepository.save(new User("user@gmail.com", "1234", "commenter"));
        Moment moment = momentRepository.save(new Moment("오런완!", true, momenter));
        Comment comment = commentRepository.save(new Comment("수고 많으셨습니다.", commenter, moment));

        emojiRepository.save(new Emoji("HEART", momenter, comment));

        // when
        List<Emoji> result = emojiRepository.findAllByComment(comment);

        // then
        Emoji emoji = result.getFirst();
        assertAll(
                () -> assertThat(emoji.getEmojiType()).isEqualTo("HEART"),
                () -> assertThat(emoji.getUser()).isEqualTo(momenter)
        );
    }
}
