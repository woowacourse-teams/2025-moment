package moment.moment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentRepositoryTest {

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @Disabled
    void 내_모멘트를_생성시간_기준_내림차순으로_정렬하여_페이지를_조회한다() throws InterruptedException {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        Moment moment1 = new Moment("아 행복해", true, savedMomenter, WriteType.BASIC);
        Moment moment2 = new Moment("아 즐거워", true, savedMomenter, WriteType.BASIC);
        Moment moment3 = new Moment("아 짜릿해", true, savedMomenter, WriteType.BASIC);
        Moment moment4 = new Moment("킥킥", true, savedMomenter, WriteType.BASIC);

        Moment savedMoment1 = momentRepository.save(moment1);
        Thread.sleep(10);
        Moment savedMoment2 = momentRepository.save(moment2);
        Thread.sleep(10);
        Moment savedMoment3 = momentRepository.save(moment3);
        Thread.sleep(10);
        Moment savedMoment4 = momentRepository.save(moment4);

        // when
        List<Moment> moments = momentRepository.findMyMomentsNextPage(momenter, savedMoment4.getCreatedAt(),
                savedMoment4.getId(), PageRequest.of(0, 3));

        // then
        assertAll(
                () -> assertThat(moments).hasSize(3),
                () -> assertThat(moments).isSortedAccordingTo(Comparator.comparing(Moment::getCreatedAt).reversed()),
                () -> assertThat(moments.getFirst()).isEqualTo(savedMoment3),
                () -> assertThat(moments.get(1)).isEqualTo(savedMoment2),
                () -> assertThat(moments.getLast()).isEqualTo(savedMoment1)
        );
    }

    @Test
    void 유저가_오늘_기본적으로_생성한_모멘트_수를_카운트한다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);
        WriteType basicWriteType = WriteType.BASIC;

        Moment basicMoment1 = new Moment("아 행복해", true, savedMomenter, WriteType.BASIC);
        Moment basicMoment2 = new Moment("아 즐거워", true, savedMomenter, WriteType.BASIC);
        Moment basicMoment3 = new Moment("아 짜릿해", true, savedMomenter, WriteType.BASIC);
        Moment extraMoment1 = new Moment("아 불행해", true, savedMomenter, WriteType.EXTRA);

        momentRepository.save(basicMoment1);
        momentRepository.save(basicMoment2);
        momentRepository.save(basicMoment3);
        momentRepository.save(extraMoment1);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        // when & then
        assertThat(momentRepository.countByMomenterAndWriteTypeAndCreatedAtBetween(momenter, basicWriteType, startOfDay,
                endOfDay)).isEqualTo(3);
    }

    @Test
    void 유저가_오늘_추가_리워드로_생성한_모멘트_수를_카운트한다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);
        WriteType extraWriteType = WriteType.EXTRA;

        Moment basicMoment1 = new Moment("아 행복해", true, savedMomenter, WriteType.BASIC);
        Moment basicMoment2 = new Moment("아 즐거워", true, savedMomenter, WriteType.BASIC);
        Moment basicMoment3 = new Moment("아 짜릿해", true, savedMomenter, WriteType.BASIC);
        Moment extraMoment1 = new Moment("아 불행해", true, savedMomenter, WriteType.EXTRA);

        momentRepository.save(basicMoment1);
        momentRepository.save(basicMoment2);
        momentRepository.save(basicMoment3);
        momentRepository.save(extraMoment1);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        // when & then
        assertThat(momentRepository.countByMomenterAndWriteTypeAndCreatedAtBetween(momenter, extraWriteType, startOfDay,
                endOfDay)).isEqualTo(1);
    }

    @Test
    void 코멘트를_달_수_있는_모멘트를_조회한다() {
        // given
        User user = userRepository.save(new User("mimi@icloud.com", "mimi1234!", "mimi", ProviderType.EMAIL));
        User other = userRepository.save(new User("hippo@gmail.com", "hippo1234!", "hippo", ProviderType.EMAIL));

        Moment myMoment = momentRepository.save(new Moment("내가 쓴 모멘트", user, WriteType.BASIC));

        Moment recentMoment = momentRepository.save(new Moment("다른 사람 모멘트", other, WriteType.BASIC));

        // TODO : 시간을 DB에서 처리하고 있어서 컨트롤 불가능

        Moment commentedMoment = momentRepository.save(new Moment("이미 코멘트를 단 모멘트", other, WriteType.BASIC));
        commentRepository.save(new Comment("희희", user, commentedMoment));

        // when
        List<Moment> results = momentRepository.findCommentableMoments(user, LocalDateTime.now().minusDays(3));

        // then
        assertThat(results).containsExactly(recentMoment);
        assertThat(results).doesNotContain(myMoment, commentedMoment);
    }

    @Test
    void 특정_기간_동안_등록한_나의_모멘트_목록을_조회한다() throws InterruptedException {
        // given
        User momenter = userRepository.save(new User("test@user.com", "password", "tester", ProviderType.EMAIL));

        Thread.sleep(10); // Ensure timestamps are distinct
        Moment newMoment1 = momentRepository.save(new Moment("1 day ago", momenter, WriteType.BASIC));
        Thread.sleep(10);
        Moment newMoment2 = momentRepository.save(new Moment("now", momenter, WriteType.BASIC));

        // when
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<Moment> recentMoments = momentRepository.findByMomenterAndCreatedAtAfter(momenter, threeDaysAgo);

        // then
        assertThat(recentMoments).hasSize(2)
                .containsExactlyInAnyOrder(newMoment1, newMoment2);
    }
}
