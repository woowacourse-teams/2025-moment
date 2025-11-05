package moment.comment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import moment.comment.domain.Comment;
import moment.comment.domain.Echo;
import moment.fixture.UserFixture;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
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
        User momenter = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("오런완!", true, momenter, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("수고 많으셨습니다.", commenter, moment.getId()));

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
    void 유저가_코멘트에_이미_에코_타입을_보낸_경우_조회하여_반환한다() {
        // given
        User momenter = UserFixture.createUser();
        userRepository.save(momenter);
        User commenter = UserFixture.createUser();
        userRepository.save(commenter);
        Moment moment = new Moment("하이", momenter, WriteType.BASIC);
        momentRepository.save(moment);
        Comment comment = new Comment("바이", commenter, moment.getId());
        commentRepository.save(comment);
        Echo existingEcho1 = new Echo("THANKS", commenter, comment);
        echoRepository.save(existingEcho1);
        Echo existingEcho2 = new Echo("COMFORTED", commenter, comment);
        echoRepository.save(existingEcho2);

        // when
        List<Echo> result = echoRepository.findByCommentAndUserAndEchoTypeIn(comment, commenter,
                Set.of("THANKS", "COMFORTED"));

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 유저가_코멘트에_다른_에코_타입을_보냈으면_조회하지_않는다() {
        // given
        User momenter = UserFixture.createUser();
        userRepository.save(momenter);
        User commenter = UserFixture.createUser();
        userRepository.save(commenter);
        Moment moment = new Moment("하이", momenter, WriteType.BASIC);
        momentRepository.save(moment);
        Comment comment = new Comment("바이", commenter, moment.getId());
        commentRepository.save(comment);
        Echo existingEcho = new Echo("THANKS", commenter, comment);
        echoRepository.save(existingEcho);

        // when
        List<Echo> result = echoRepository.findByCommentAndUserAndEchoTypeIn(comment, commenter, Set.of("COMFORTED"));

        // then
        assertThat(result).hasSize(0);
    }

    @Test
    void 코멘트_리스트에_포함된_모든_에코를_조회한다() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment content", true, user1, WriteType.BASIC));
        Comment comment1 = commentRepository.save(new Comment("comment 1", user1, moment.getId()));
        Comment comment2 = commentRepository.save(new Comment("comment 2", user2, moment.getId()));
        Comment comment3 = commentRepository.save(new Comment("comment 3", user1, moment.getId()));

        Echo echo1 = echoRepository.save(new Echo("HEART", user2, comment1));
        Echo echo2 = echoRepository.save(new Echo("THANKS", user1, comment2));
        echoRepository.save(new Echo("SAD", user2, comment3));

        List<Comment> commentsToSearch = List.of(comment1, comment2);

        // when
        List<Echo> result = echoRepository.findAllByCommentIn(commentsToSearch);

        // then
        assertThat(result).hasSize(2)
                .containsExactlyInAnyOrder(echo1, echo2);
    }

    @Test
    void 코멘트로_에코를_조회한다() {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("오런완!", true, momenter, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("수고 많으셨습니다.", commenter, moment.getId()));

        Echo savedEcho = echoRepository.save(new Echo("HEART", momenter, comment));

        // when
        Optional<Echo> result = echoRepository.findByComment(comment);

        // then
        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get()).isEqualTo(savedEcho)
        );
    }

    @Test
    void 코멘트에_에코가_없으면_빈_Optional을_반환한다() {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("오런완!", true, momenter, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("수고 많으셨습니다.", commenter, moment.getId()));

        // when
        Optional<Echo> result = echoRepository.findByComment(comment);

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    void 코멘트_ID로_에코를_삭제한다() {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("오런완!", true, momenter, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("수고 많으셨습니다.", commenter, moment.getId()));
        Long commentId = comment.getId();

        echoRepository.save(new Echo("HEART", momenter, comment));
        echoRepository.save(new Echo("THANKS", commenter, comment));

        // when
        echoRepository.deleteByCommentId(commentId);

        // then
        List<Echo> result = echoRepository.findAllByComment(comment);
        assertThat(result).isEmpty();
    }
}
