package moment.reward.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import moment.reward.domain.PointHistory;
import moment.reward.domain.Reason;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
        User user = userRepository.save(new User("drago@gmail.com", "1q2w3e4r!", "drago"));
        Reason reason = Reason.POSITIVE_EMOJI_RECEIVED;
        Long contentId = 1L;

        rewardRepository.save(new PointHistory(user, reason.getPointTo(), reason, contentId));

        // when
        boolean result = rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 존재하지_않는_포인트_증감_기록이면_false_반환한다() {
        // given
        User user = userRepository.save(new User("drago@gmail.com", "1q2w3e4r!", "drago"));
        Reason reason = Reason.POSITIVE_EMOJI_RECEIVED;
        Long contentId = 1L;

        // when
        boolean result = rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId);

        // then
        assertThat(result).isFalse();
    }
}