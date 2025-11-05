package moment.comment.service.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Map;
import java.util.Set;
import moment.comment.domain.Comment;
import moment.comment.domain.Echo;
import moment.comment.infrastructure.CommentRepository;
import moment.comment.infrastructure.EchoRepository;
import moment.fixture.UserFixture;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class EchoServiceTest {

    @Autowired
    private EchoRepository echoRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserRepository userRepository;

    private EchoService echoService;

    @BeforeEach
    void setUp() {
        echoService = new EchoService(echoRepository);
    }

    @Test
    void 코멘트_목록에_대한_에코_목록을_조회한다() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", user1, WriteType.BASIC));

        Comment comment1 = commentRepository.save(new Comment("comment1", user2, moment.getId()));
        Comment comment2 = commentRepository.save(new Comment("comment2", user1, moment.getId()));
        Comment comment3 = commentRepository.save(new Comment("comment3", user2, moment.getId()));

        Echo echo1 = echoRepository.save(new Echo("HEART", user1, comment1));
        Echo echo2 = echoRepository.save(new Echo("THANKS", user2, comment1));
        Echo echo3 = echoRepository.save(new Echo("SAD", user1, comment2));

        // when
        Map<Comment, List<Echo>> result = echoService.getEchosOfComments(List.of(comment1, comment2, comment3));

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result.get(comment1)).hasSize(2).containsExactlyInAnyOrder(echo1, echo2),
                () -> assertThat(result.get(comment2)).hasSize(1).containsExactlyInAnyOrder(echo3),
                () -> assertThat(result.get(comment3)).isEmpty()
        );
    }

    @Test
    void 코멘트_ID로_에코를_삭제한다() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", user1, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("comment", user2, moment.getId()));

        echoRepository.save(new Echo("HEART", user1, comment));
        echoRepository.save(new Echo("THANKS", user2, comment));

        assertThat(echoRepository.count()).isEqualTo(2);

        // when
        echoService.deleteBy(comment.getId());

        // then
        assertThat(echoRepository.count()).isZero();
    }

    @Test
    void 새로운_에코만_저장한다() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("comment", commenter, moment.getId()));

        // commenter는 이미 "HEART" 에코를 보냈습니다.
        echoRepository.save(new Echo("HEART", commenter, comment));
        assertThat(echoRepository.count()).isEqualTo(1);

        // when
        // commenter가 이제 "HEART"와 "THANKS"를 보냅니다.
        echoService.saveIfNotExisted(comment, commenter, Set.of("HEART", "THANKS"));

        // then
        // "THANKS"만 새로 저장되어야 합니다.
        List<Echo> echos = echoRepository.findAllByComment(comment);
        assertThat(echos).hasSize(2);
        assertThat(echos.stream().map(Echo::getEchoType)).contains("HEART", "THANKS");
    }

    @Test
    void 기존에_에코가_없을_때_모든_에코를_저장한다() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("comment", commenter, moment.getId()));

        assertThat(echoRepository.count()).isZero();

        // when
        echoService.saveIfNotExisted(comment, commenter, Set.of("HEART", "THANKS"));

        // then
        List<Echo> echos = echoRepository.findAllByComment(comment);
        assertThat(echos).hasSize(2);
    }

    @Test
    void 이미_모든_에코가_존재할_때_저장하지_않는다() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("comment", commenter, moment.getId()));

        echoRepository.save(new Echo("HEART", commenter, comment));
        echoRepository.save(new Echo("THANKS", commenter, comment));
        assertThat(echoRepository.count()).isEqualTo(2);

        // when
        echoService.saveIfNotExisted(comment, commenter, Set.of("HEART", "THANKS"));

        // then
        assertThat(echoRepository.count()).isEqualTo(2);
    }

    @Test
    void 코멘트로_에코_목록을_조회한다() {
        // given
        User momentOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentRepository.save(new Moment("moment", momentOwner, WriteType.BASIC));

        Comment comment1 = commentRepository.save(new Comment("comment1", commenter, moment.getId()));
        Comment comment2 = commentRepository.save(new Comment("comment2", commenter, moment.getId()));

        Echo echo1 = echoRepository.save(new Echo("HEART", commenter, comment1));
        Echo echo2 = echoRepository.save(new Echo("THANKS", commenter, comment1));
        echoRepository.save(new Echo("SAD", commenter, comment2)); // 이 에코는 결과에 포함되면 안 됩니다.

        // when
        List<Echo> result = echoService.getEchosBy(comment1);

        // then
        assertThat(result).hasSize(2)
                .containsExactlyInAnyOrder(echo1, echo2);
    }
}
