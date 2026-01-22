package moment.moment.service.moment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.domain.BaseEntity;
import moment.global.exception.MomentException;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.support.MomentCreatedAtHelper;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentServiceTest {

    @Autowired
    MomentService momentService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    MomentCreatedAtHelper momentCreatedAtHelper;

    private User momenter;

    @BeforeEach
    void setUp() {
        User user = UserFixture.createUser();
        momenter = userRepository.save(user);
    }

    @Test
    void 모멘트를_생성한다() {
        // given
        String content = "hello!";

        // when
        Moment moment = momentService.create(content, momenter);

        // then
        assertAll(
                () -> assertThat(moment.getContent()).isEqualTo(content),
                () -> assertThat(moment.getMomenter()).isEqualTo(momenter)
        );
    }

    @Test
    void 나의_모멘트_첫_페이지를_조회한다() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment moment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment1", momenter, start);
        Moment moment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment2", momenter,
                start.plusHours(1));
        Moment moment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment3", momenter,
                start.plusHours(2));

        Cursor cursor = new Cursor(null);
        PageSize pageSize = new PageSize(2);

        // when
        List<Moment> momentsWithinCursor = momentService.getMomentsBy(momenter, cursor, pageSize);

        // then
        assertAll(
                () -> assertThat(momentsWithinCursor).hasSize(3),
                () -> assertThat(momentsWithinCursor.get(0).getContent()).isEqualTo(moment3.getContent()),
                () -> assertThat(momentsWithinCursor.get(1).getContent()).isEqualTo(moment2.getContent()),
                () -> assertThat(momentsWithinCursor.get(2).getContent()).isEqualTo(moment1.getContent())
        );
    }

    @Test
    void 나의_모멘트_다음_페이지를_조회한다() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment moment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment1", momenter, start);
        Moment moment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment2", momenter,
                start.plusHours(1));
        Moment moment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment3", momenter,
                start.plusHours(2));

        Cursor cursor = new Cursor(moment3.getCreatedAt().toString() + "_" + moment3.getId());
        PageSize pageSize = new PageSize(2);

        // when
        List<Moment> momentsWithinCursor = momentService.getMomentsBy(momenter, cursor, pageSize);

        // then
        assertAll(
                () -> assertThat(momentsWithinCursor).hasSize(2),
                () -> assertThat(momentsWithinCursor.get(0).getContent()).isEqualTo(moment2.getContent()),
                () -> assertThat(momentsWithinCursor.get(1).getContent()).isEqualTo(moment1.getContent())
        );
    }

    @Test
    void 읽지_않은_모멘트_첫_페이지를_조회한다() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment unReadMoment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment1", momenter,
                start.plusHours(1));
        Moment unReadMoment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment2", momenter,
                start.plusHours(2));
        Moment readMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("moment3", momenter,
                start.plusHours(3));
        Moment unReadMoment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment4", momenter,
                start.plusHours(4));

        Cursor cursor = new Cursor(null);
        PageSize pageSize = new PageSize(2);

        List<Long> unreadMomentIds = List.of(unReadMoment1, unReadMoment2, unReadMoment3).stream()
                .map(Moment::getId)
                .toList();

        // when
        List<Moment> momentsWithinCursor = momentService.getUnreadMomentsBy(unreadMomentIds, cursor, pageSize);

        // then
        assertAll(
                () -> assertThat(momentsWithinCursor).hasSize(3),
                () -> assertThat(momentsWithinCursor.get(0).getContent()).isEqualTo(unReadMoment3.getContent()),
                () -> assertThat(momentsWithinCursor.get(1).getContent()).isEqualTo(unReadMoment2.getContent()),
                () -> assertThat(momentsWithinCursor.get(2).getContent()).isEqualTo(unReadMoment1.getContent())
        );
    }

    @Test
    void 읽지_않은_모멘트_다음_페이지를_조회한다() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment unReadMoment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment1", momenter,
                start.plusHours(1));
        Moment unReadMoment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment2", momenter,
                start.plusHours(2));
        Moment readMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("moment3", momenter,
                start.plusHours(3));
        Moment unReadMoment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment4", momenter,
                start.plusHours(4));

        Cursor cursor = new Cursor(unReadMoment2.getCreatedAt() + "_" + unReadMoment2.getId());
        PageSize pageSize = new PageSize(2);

        List<Long> unreadMomentIds = List.of(unReadMoment1, unReadMoment2, unReadMoment3).stream()
                .map(Moment::getId)
                .toList();

        // when
        List<Moment> momentsWithinCursor = momentService.getUnreadMomentsBy(unreadMomentIds, cursor, pageSize);

        // then
        assertAll(
                () -> assertThat(momentsWithinCursor).hasSize(1),
                () -> assertThat(momentsWithinCursor.get(0).getContent()).isEqualTo(unReadMoment1.getContent())
        );
    }

    @Test
    void 신고한_모멘트를_제외하고_코멘트_달_수_있는_모멘트를_조회한다() {
        // given
        Moment moment1 = momentRepository.save(new Moment("moment1", momenter));
        Moment moment2 = momentRepository.save(new Moment("moment2", momenter));
        Moment moment3 = momentRepository.save(new Moment("moment3", momenter));

        User reporter = userRepository.save(
                UserFixture.createUser()
        );

        List<Long> reportedMomentIds = List.of(moment1, moment3).stream()
                .map(Moment::getId)
                .toList();

        // when
        List<Moment> commentableMoments = momentService.getCommentableMoments(reporter, reportedMomentIds);

        // then
        assertAll(
                () -> assertThat(commentableMoments).hasSize(1),
                () -> assertThat(commentableMoments.get(0)).isEqualTo(moment2)
        );
    }

    @Test
    @Disabled
    void 기간이_지난_모멘트를_제외하고_달_수_있는_모멘트를_조회한다() throws NoSuchFieldException {
        // given
        Moment newMoment1 = momentRepository.save(new Moment("moment1", momenter));
        Moment newMoment2 = momentRepository.save(new Moment("moment2", momenter));
        Moment oldMoment = momentRepository.save(new Moment("I wrote it 5days ago", momenter));

        Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        ReflectionUtils.setField(createdAtField, oldMoment, LocalDateTime.now().minusDays(5));
        momentRepository.flush();

        User user = userRepository.save(UserFixture.createUser());
        List<Long> reportedMomentIds = List.of();

        // when
        List<Moment> commentableMoments = momentService.getCommentableMoments(user, reportedMomentIds);

        // then
        assertAll(
                () -> assertThat(commentableMoments).hasSize(2),
                () -> assertThat(commentableMoments).containsExactlyInAnyOrder(newMoment1, newMoment2)
        );
    }

    @Test
    void 모멘트_아이디로_모멘트를_조회한다() {
        // given
        Moment moment1 = momentRepository.save(new Moment("moment1", momenter));
        Moment moment2 = momentRepository.save(new Moment("moment2", momenter));
        Moment moment3 = momentRepository.save(new Moment("moment3", momenter));

        List<Long> momentIds = List.of(moment1, moment2, moment3).stream()
                .map(Moment::getId)
                .toList();

        // when
        List<Moment> momentsBy = momentService.getMomentsBy(momentIds);

        // then
        assertThat(momentsBy).hasSize(3);
    }

    @Test
    void 모멘트를_삭제한다() {
        // given
        Moment moment1 = momentRepository.save(new Moment("moment1", momenter));
        Moment moment2 = momentRepository.save(new Moment("moment2", momenter));

        // when
        momentService.deleteBy(moment1.getId());

        // then
        List<Moment> allMoments = momentRepository.findAll();
        assertAll(
                () -> assertThat(allMoments).hasSize(1),
                () -> assertThat(allMoments.get(0)).isEqualTo(moment2)
        );
    }

    @Test
    void 모멘트가_존재하는지_확인한다() {
        // given
        Moment moment = momentRepository.save(new Moment("moment1", momenter));

        // when
        boolean isExistsMoment1 = momentService.existsMoment(moment.getId());
        boolean isExistsMoment2 = momentService.existsMoment(2L);

        // then
        assertAll(
                () -> assertThat(isExistsMoment1).isTrue(),
                () -> assertThat(isExistsMoment2).isFalse()
        );
    }

    @Test
    void 모멘트의_작성자가_맞을경우_예외가_발생하지_않는다() {
        // given
        Moment moment = momentRepository.save(new Moment("moment1", momenter));

        // when & then
        assertDoesNotThrow(() -> momentService.validateMomenter(moment.getId(), momenter));
    }

    @Test
    void 모멘트의_작성자가_아니면_예외가_발생한다() {
        // given
        Moment moment = momentRepository.save(new Moment("moment1", momenter));

        User notMomenter = UserFixture.createUser();
        momenter = userRepository.save(notMomenter);

        // when & then
        assertThatThrownBy(() -> momentService.validateMomenter(moment.getId(), notMomenter))
                .isInstanceOf(MomentException.class);
    }
}
