package moment.moment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import moment.fixture.UserFixture;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.support.MomentCreatedAtHelper;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@Import(MomentCreatedAtHelper.class)
@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentRepositoryTest {

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentCreatedAtHelper momentCreatedAtHelper;

    @Test
    void 내_모멘트를_생성시간_기준_내림차순으로_정렬하여_페이지를_조회한다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);

        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment savedMoment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 행복해", savedMomenter, WriteType.BASIC,
                start);
        Moment savedMoment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 즐거워", savedMomenter, WriteType.BASIC,
                start.plusHours(1));
        Moment savedMoment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 짜릿해", savedMomenter, WriteType.BASIC,
                start.plusHours(2));
        Moment savedMoment4 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 킥킥", savedMomenter, WriteType.BASIC,
                start.plusHours(3));

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
        User momenter = UserFixture.createUser();
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
        User momenter = UserFixture.createUser();
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
    void 다른_사람이_작성한_3일이내의_모멘트를_조회한다() {
        // given
        User user = userRepository.save(UserFixture.createUser());
        User other = userRepository.save(UserFixture.createUser());

        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment myMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("내가 쓴 모멘트", user, WriteType.BASIC, start);

        Moment recentMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("다른 사람 최신 모멘트", other, WriteType.BASIC,
                start.minusDays(3));

        Moment oldMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("다른 사람 4일전 모멘트", other, WriteType.BASIC,
                start.minusDays(4));

        Moment reportedMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("신고한 모멘트", other, WriteType.BASIC,
                start.plusHours(2));
        List<Long> reportedMomentIds = List.of(reportedMoment.getId());

        // when
        List<Long> results = momentRepository.findMomentIdsExcludingReported(user.getId(), start.minusDays(3),
                reportedMomentIds);

        // then
        assertThat(results).containsExactly(recentMoment.getId());
        assertThat(results).doesNotContain(myMoment.getId(), oldMoment.getId());
    }

    @Test
    void 읽지_않은_모멘트_목록의_첫_페이지를_조회한다() {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        Moment moment1 = momentRepository.save(new Moment("moment1", momenter, WriteType.BASIC));
        Moment moment2 = momentRepository.save(new Moment("moment2", momenter, WriteType.BASIC));
        momentRepository.save(new Moment("moment3", momenter, WriteType.BASIC)); // This one is not unread

        // when
        List<Moment> result = momentRepository.findMyUnreadMomentFirstPage(List.of(moment1.getId(), moment2.getId()),
                PageRequest.of(0, 5));

        // then
        assertThat(result).hasSize(2)
                .containsExactlyInAnyOrder(moment1, moment2);
    }

    @Test
    void 읽지_않은_모멘트_목록의_두_번째_페이지를_조회한다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);

        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment savedMoment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 행복해", savedMomenter, WriteType.BASIC,
                start);
        Moment savedMoment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 즐거워", savedMomenter, WriteType.BASIC,
                start.plusHours(1));
        Moment savedMoment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 짜릿해", savedMomenter, WriteType.BASIC,
                start.plusHours(2));

        // when
        List<Moment> result = momentRepository.findMyUnreadMomentNextPage(
                List.of(savedMoment1.getId(), savedMoment2.getId(), savedMoment3.getId()),
                savedMoment3.getCreatedAt(),
                savedMoment3.getId(),
                PageRequest.of(0, 1));

        // then
        assertThat(result).hasSize(1)
                .containsExactly(savedMoment2);
    }

    @Test
    void 내_모멘트_목록의_첫_페이지를_조회한다() throws InterruptedException {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        User other = userRepository.save(UserFixture.createUser());

        momentRepository.save(new Moment("다른 사람 모멘트", other, WriteType.BASIC));
        Moment moment1 = momentRepository.save(new Moment("아 행복해", momenter, WriteType.BASIC));
        Thread.sleep(10);
        Moment moment2 = momentRepository.save(new Moment("아 즐거워", momenter, WriteType.BASIC));
        Thread.sleep(10);
        Moment moment3 = momentRepository.save(new Moment("아 짜릿해", momenter, WriteType.BASIC));

        PageRequest pageRequest = PageRequest.of(0, 2);

        // when
        List<Moment> moments = momentRepository.findMyMomentFirstPage(momenter, pageRequest);

        // then
        assertAll(
                () -> assertThat(moments).hasSize(2),
                () -> assertThat(moments).isSortedAccordingTo(Comparator.comparing(Moment::getCreatedAt).reversed()),
                () -> assertThat(moments.get(0)).isEqualTo(moment3),
                () -> assertThat(moments.get(1)).isEqualTo(moment2)
        );
    }

    @Test
    void 모멘트_ID로_모멘트를_삭제한다() {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        Moment momentToDelete = momentRepository.save(new Moment("삭제될 모멘트", momenter, WriteType.BASIC));
        Moment momentToKeep = momentRepository.save(new Moment("유지될 모멘트", momenter, WriteType.BASIC));

        // when
        momentRepository.deleteById(momentToDelete.getId());

        // then
        assertAll(
                () -> assertThat(momentRepository.findById(momentToDelete.getId())).isEmpty(),
                () -> assertThat(momentRepository.findById(momentToKeep.getId())).isPresent()
        );
    }

    @Test
    void 모멘트_ID로_조회할_때_momenter를_함께_조회한다() {
        // given
        User momenter1 = userRepository.save(UserFixture.createUser());
        User momenter2 = userRepository.save(UserFixture.createUser());

        Moment moment1 = momentRepository.save(new Moment("첫번째 모멘트", momenter1, WriteType.BASIC));
        Moment moment2 = momentRepository.save(new Moment("두번째 모멘트", momenter2, WriteType.BASIC));
        Moment moment3 = momentRepository.save(new Moment("세번째 모멘트", momenter1, WriteType.BASIC));

        List<Long> idsToFetch = List.of(moment1.getId(), moment3.getId());

        // when
        List<Moment> results = momentRepository.findAllWithMomenterByIds(idsToFetch);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Moment::getId).containsExactlyInAnyOrderElementsOf(idsToFetch);
        assertThat(results).allSatisfy(m -> assertThat(m.getMomenter()).isNotNull());
        assertThat(results)
                .extracting(m -> m.getMomenter().getEmail())
                .containsExactlyInAnyOrder(momenter1.getEmail(), momenter1.getEmail());
    }
}
