package moment.moment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import moment.matching.domain.Matching;
import moment.matching.infrastructure.MatchingRepository;
import moment.moment.domain.Moment;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentRepositoryTest {

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchingRepository matchingRepository;

    @Test
    void 내_모멘트를_생성시간_기준_내림차순으로_정렬한다() throws InterruptedException {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo");
        User savedMomenter = userRepository.save(momenter);

        Moment moment1 = new Moment("아 행복해", true, savedMomenter);
        Moment moment2 = new Moment("아 즐거워", true, savedMomenter);
        Moment moment3 = new Moment("아 짜릿해", true, savedMomenter);
        Moment moment4 = new Moment("킥킥", true, savedMomenter);

        momentRepository.save(moment1);
        Thread.sleep(10);
        momentRepository.save(moment2);
        Thread.sleep(10);
        momentRepository.save(moment3);
        Thread.sleep(10);
        momentRepository.save(moment4);

        // when
        List<Moment> moments = momentRepository.findMomentByMomenterOrderByCreatedAtDesc(momenter);

        // then
        assertThat(moments).hasSize(4);
        assertThat(moments).isSortedAccordingTo(Comparator.comparing(Moment::getCreatedAt).reversed());
    }

    @Test
    void 나에게_매칭된_모멘트를_조회한다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo");
        User savedMomenter = userRepository.save(momenter);

        User commenter = new User("kiki@gmail.com", "1234", "kiki");
        User savedCommenter = userRepository.save(commenter);

        Moment moment = new Moment("아 행복해", true, savedMomenter);
        Moment savedMoment = momentRepository.save(moment);

        Matching matching = new Matching(moment, commenter);
        Matching savedMatching = matchingRepository.save(matching);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        // when
        Optional<Moment> matchedMomentByCommenter = momentRepository.findMatchedMomentByCommenter(savedCommenter,
                startOfDay, endOfDay);

        // then
        assertAll(
                () -> assertThat(matchedMomentByCommenter).isPresent(),
                () -> assertThat(matchedMomentByCommenter.get().getId()).isEqualTo(savedMoment.getId())
        );
    }
}
