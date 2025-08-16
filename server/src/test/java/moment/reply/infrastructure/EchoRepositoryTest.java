package moment.reply.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.reply.domain.Echo;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class EchoRepositoryTest {

    @Autowired
    EchoRepository echoRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    CommentRepository commentRepository;

    @Test
    void 코멘트에_달린_모든_에코를_조회한다() {
        // given
        User momenter = userRepository.save(new User("ekorea623@gmail.com", "1q2w3e4r", "drago", ProviderType.EMAIL));
        User commenter = userRepository.save(new User("user@gmail.com", "1234", "user", ProviderType.EMAIL));
        Moment moment = momentRepository.save(new Moment("오런완!", true, momenter, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("수고 많으셨습니다.", commenter, moment));

        echoRepository.save(new Echo("HEART", momenter, comment));

        // when
        List<Echo> result = echoRepository.findAllByComment(comment);

        // then
        Echo echo = result.getFirst();
        assertAll(
                () -> assertThat(echo.getEchoType()).isEqualTo("HEART"),
                () -> assertThat(echo.getUser()).isEqualTo(momenter)
        );
    }

    @Test
    void 유저가_코멘트에_이미_에코_타입을_보냈으면_참을_반환한다() {
        // given
        User momenter = new User("cookie@gmail.com", "cookie1234!", "쿠키", ProviderType.EMAIL);
        userRepository.save(momenter);
        User commenter = new User("mimi@icloud.com", "mimi1234!", "미미", ProviderType.EMAIL);
        userRepository.save(commenter);
        Moment moment = new Moment("하이", momenter, WriteType.BASIC);
        momentRepository.save(moment);
        Comment comment = new Comment("바이", commenter, moment);
        commentRepository.save(comment);
        Echo existingEcho = new Echo("THANKS", commenter, comment);
        echoRepository.save(existingEcho);

        // when
        boolean result = echoRepository.existsByCommentAndUserAndEchoType(comment, commenter, "THANKS");

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 유저가_코멘트에_다른_에코_타입을_보냈으면_거짓을_반환한다() {
        // given
        User momenter = new User("cookie@gmail.com", "cookie1234!", "쿠키", ProviderType.EMAIL);
        userRepository.save(momenter);
        User commenter = new User("mimi@icloud.com", "mimi1234!", "미미", ProviderType.EMAIL);
        userRepository.save(commenter);
        Moment moment = new Moment("하이", momenter, WriteType.BASIC);
        momentRepository.save(moment);
        Comment comment = new Comment("바이", commenter, moment);
        commentRepository.save(comment);
        Echo existingEcho = new Echo("THANKS", commenter, comment);
        echoRepository.save(existingEcho);

        // when
        boolean result = echoRepository.existsByCommentAndUserAndEchoType(comment, commenter, "COMFORTED");

        // then
        assertThat(result).isFalse();
    }
}
