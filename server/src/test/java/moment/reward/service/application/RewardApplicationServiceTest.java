package moment.reward.service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.reward.infrastructure.RewardRepository;
import moment.user.domain.User;
import moment.user.dto.response.MyRewardHistoryPageResponse;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class RewardApplicationServiceTest {

    @Autowired
    private RewardApplicationService rewardApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RewardRepository rewardRepository;

    private User user;

    @BeforeEach
    void setUp() {
        User newUser = UserFixture.createUser();
        user = userRepository.save(newUser);
    }

    @Test
    void 코멘트_작성_시_보상이_지급되고_기록이_남는다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        Long commentId = 1L;

        // when
        rewardApplicationService.rewardForComment(user.getId(), reason, commentId);

        // then
        User foundUser = userRepository.findById(user.getId()).get();
        boolean historyExists = rewardRepository.existsByUserAndReasonAndContentId(user, reason, commentId);

        assertAll(
                () -> assertThat(foundUser.getAvailableStar()).isEqualTo(reason.getPointTo()),
                () -> assertThat(historyExists).isTrue()
        );
    }

    @Test
    void 이미_보상받은_코멘트는_중복으로_보상이_지급되지_않는다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        Long commentId = 1L;
        rewardApplicationService.rewardForComment(user.getId(), reason, commentId); // 첫 번째 보상 지급

        // when
        rewardApplicationService.rewardForComment(user.getId(), reason, commentId); // 중복 호출

        // then
        User foundUser = userRepository.findById(user.getId()).get();

        assertThat(foundUser.getAvailableStar()).isEqualTo(reason.getPointTo());
    }

    @Test
    void 별조각을_사용하면_차감되고_사용_기록이_남는다() {
        // given
        user.addStarAndUpdateLevel(100); // 테스트를 위해 별조각을 미리 지급
        userRepository.save(user);

        Reason reason = Reason.MOMENT_ADDITIONAL_USE; // -5점
        Long contentId = 1L;
        int expectedStars = 100 + reason.getPointTo(); // 100 - 5 = 95

        // when
        rewardApplicationService.useReward(user.getId(), reason, contentId);

        // then
        User foundUser = userRepository.findById(user.getId()).get();
        boolean historyExists = rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId);

        assertAll(
                () -> assertThat(foundUser.getAvailableStar()).isEqualTo(expectedStars),
                () -> assertThat(historyExists).isTrue()
        );
    }

    @Test
    void 보유한_별조각이_부족하면_예외가_발생한다() {
        // given
        Reason reason = Reason.MOMENT_ADDITIONAL_USE;
        Long contentId = 1L;

        // when & then
        assertThatThrownBy(() -> rewardApplicationService.useReward(user.getId(), reason, contentId))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_ENOUGH_STAR);
    }

    @Test
    void 유저의_보상_내역을_페이지로_조회한다() {
        // given
        int pageNum = 0;
        int pageSize = 2;

        rewardRepository.save(new RewardHistory(user, Reason.MOMENT_CREATION, 1L));
        rewardRepository.save(new RewardHistory(user, Reason.COMMENT_CREATION, 2L));
        rewardRepository.save(new RewardHistory(user, Reason.ECHO_RECEIVED, 3L));

        // when
        MyRewardHistoryPageResponse resultPage = rewardApplicationService.getRewardHistoryBy(user.getId(), pageNum,
                pageSize);

        // then
        assertAll(
                () -> assertThat(resultPage.totalPages()).isEqualTo(2),
                () -> assertThat(resultPage.items()).hasSize(2),
                () -> assertThat(resultPage.items().get(0).reason()).isEqualTo(Reason.ECHO_RECEIVED)
        );
    }
}
