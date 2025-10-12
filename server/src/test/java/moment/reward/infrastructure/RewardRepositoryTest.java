package moment.reward.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Comparator;
import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class RewardRepositoryTest {

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 이미_존재하는_포인트_증감_기록이면_true_반환한다() {
        // given
        User user = userRepository.save(new User("drago@gmail.com", "1q2w3e4r!", "drago", ProviderType.EMAIL));
        Reason reason = Reason.ECHO_RECEIVED;
        Long contentId = 1L;

        rewardRepository.save(new RewardHistory(user, reason, contentId));

        // when
        boolean result = rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 존재하지_않는_포인트_증감_기록이면_false_반환한다() {
        // given
        User user = userRepository.save(new User("drago@gmail.com", "1q2w3e4r!", "drago", ProviderType.EMAIL));
        Reason reason = Reason.ECHO_RECEIVED;
        Long contentId = 1L;

        // when
        boolean result = rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 유저의_보상_기록을_CreatedAt_내림차순으로_조회한다() {
        // given
        User user = new User("test@gmail.com", "qwer1234!", "신비로운 행성의 지구", ProviderType.EMAIL);

        userRepository.save(user);

        createTestRewardHistory(user);

        Page<RewardHistory> page = rewardRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, 10));

        // when & then
        assertAll(
                () -> assertThat(page.getTotalPages()).isEqualTo(2),
                () -> assertThat(page.getSize()).isEqualTo(10),
                () -> assertThat(page.getContent().size()).isEqualTo(10),
                () -> assertThat(page.getNumber()).isEqualTo(0),
                () -> assertThat(page.getContent())
                        .extracting(RewardHistory::getCreatedAt)
                        .isSortedAccordingTo(Comparator.reverseOrder())
        );
    }

    private void createTestRewardHistory(User user) {
        for (int i = 0; i < 20; i++) {
            rewardRepository.save(
                    new RewardHistory(user, Reason.MOMENT_CREATION, (long) i));
        }
    }
}
