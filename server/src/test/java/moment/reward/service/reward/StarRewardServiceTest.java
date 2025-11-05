package moment.reward.service.reward;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.reward.infrastructure.RewardRepository;
import moment.user.domain.User;
import moment.user.dto.response.MyRewardHistoryPageResponse;
import moment.user.dto.response.MyRewardHistoryResponse;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class StarRewardServiceTest {

    @Autowired
    StarRewardService starRewardService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RewardRepository rewardRepository;

    @MockitoBean
    Clock clock;

    private User user;

    @BeforeEach
    void setUp() {
        user = UserFixture.createUser();
        userRepository.save(user);

        Clock fixedClock = Clock.fixed(Instant.parse("2025-10-13T10:00:00Z"), ZoneId.of("UTC"));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    void 별조각_보상_기록을_저장한다() {
        // given
        Reason reason = Reason.MOMENT_CREATION;
        Long momentId = 1L;

        // when
        RewardHistory savedRewardHistory = starRewardService.save(user, reason, momentId);

        // then
        assertAll(
                () -> assertThat(savedRewardHistory.getAmount()).isEqualTo(reason.getPointTo()),
                () -> assertThat(savedRewardHistory.getReason()).isEqualTo(reason)
        );
    }

    @Test
    void 모멘트_작성에_별조각을_보상으로_지급한다() {
        // given
        Reason reason = Reason.MOMENT_CREATION;
        long momentId = 1L;

        // when
        starRewardService.rewardForMoment(user, reason, momentId);

        // then
        assertThat(user.getAvailableStar()).isEqualTo(reason.getPointTo());
    }

    @Test
    @Disabled
    void 하루_한번_모멘트_보상이_이미_지급된_경우_별조각_보상이_지급되지_않는다() {
        // given
        Reason reason = Reason.MOMENT_CREATION;
        Long momentId = 1L;

        starRewardService.rewardForMoment(user, reason, momentId);

        // when
        starRewardService.rewardForMoment(user, reason, momentId);

        // then
        assertThat(user.getAvailableStar()).isEqualTo(reason.getPointTo());
    }

    @Test
    @Disabled
    void 다음날_모멘트를_작성하면_별조각_보상이_지급된다() {
        // given
        Reason reason = Reason.MOMENT_CREATION;
        Long momentId = 1L;

        starRewardService.rewardForMoment(user, reason, momentId);
        int afterFirstRewardPoint = user.getAvailableStar();

        // when
        Clock nextDayClock = Clock.fixed(Instant.parse("2025-10-14T10:00:00Z"), ZoneId.of("UTC"));
        when(clock.instant()).thenReturn(nextDayClock.instant());
        when(clock.getZone()).thenReturn(nextDayClock.getZone());

        starRewardService.rewardForMoment(user, reason, momentId);

        // then
        int afterSecondRewardPoint = user.getAvailableStar();
        assertAll(
                () -> assertThat(afterFirstRewardPoint).isEqualTo(reason.getPointTo()),
                () -> assertThat(afterSecondRewardPoint).isEqualTo(reason.getPointTo() * 2)
        );
    }

    @Test
    void 코멘트_작성에_별조각을_보상으로_지급한다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        Long commentId = 1L;

        // when
        starRewardService.rewardForComment(user, reason, commentId);

        // then
        assertThat(user.getAvailableStar()).isEqualTo(reason.getPointTo());
    }

    @Test
    void 이미_코멘트를_작성한_경우_별조각_보상이_지급되지_않는다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        Long commentId = 1L;

        starRewardService.rewardForComment(user, reason, commentId);

        // when
        starRewardService.rewardForComment(user, reason, commentId);

        // then
        assertThat(user.getAvailableStar()).isEqualTo(reason.getPointTo());
    }

    @Test
    void 코멘트에_에코_등록시_별조각을_보상으로_지급한다() {
        // given
        Reason reason = Reason.ECHO_RECEIVED;
        Long commentId = 1L;

        // when
        starRewardService.rewardForEcho(user, reason, commentId);

        // then
        assertThat(user.getAvailableStar()).isEqualTo(reason.getPointTo());
    }

    @Test
    void 이미_에코를_등록시_별조각_보상을_지급하지_않는다() {
        // given
        Reason reason = Reason.ECHO_RECEIVED;
        Long commentId = 1L;

        starRewardService.rewardForEcho(user, reason, commentId);

        // when
        starRewardService.rewardForEcho(user, reason, commentId);

        // then
        assertThat(user.getAvailableStar()).isEqualTo(reason.getPointTo());
    }

    @Test
    void 별조각을_소모한다() {
        // given
        int startPoint = 10;
        user.addStarAndUpdateLevel(startPoint);

        Reason reason = Reason.MOMENT_ADDITIONAL_USE;
        Long momentId = 1L;

        // when
        starRewardService.useReward(user, reason, momentId);

        // then
        assertThat(user.getAvailableStar()).isEqualTo(startPoint + reason.getPointTo());
    }

    @Test
    void 유저의_별조각이_부족하면_예외가_발생한다() {
        // given
        Reason reason = Reason.MOMENT_ADDITIONAL_USE;
        Long momentId = 1L;

        // when & then
        assertThatThrownBy(() -> starRewardService.useReward(user, reason, momentId))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_ENOUGH_STAR);
    }

    @Test
    void 유저의_별조각_기록을_조회한다() {
        // given
        int pageNum = 0;
        int pageSize = 2;

        rewardRepository.save(new RewardHistory(user, Reason.MOMENT_CREATION, 1L));
        rewardRepository.save(new RewardHistory(user, Reason.COMMENT_CREATION, 1L));
        rewardRepository.save(new RewardHistory(user, Reason.ECHO_RECEIVED, 1L));

        // when
        MyRewardHistoryPageResponse rewardHistoryByUser = starRewardService.getRewardHistoryByUser(user, pageNum,
                pageSize);

        // then
        List<MyRewardHistoryResponse> items = rewardHistoryByUser.items();
        assertAll(
                () -> assertThat(items).hasSize(2),
                () -> assertThat(items)
                        .extracting(MyRewardHistoryResponse::reason)
                        .containsExactly(Reason.ECHO_RECEIVED, Reason.COMMENT_CREATION)
        );
    }
}
