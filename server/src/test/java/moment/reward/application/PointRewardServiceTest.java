package moment.reward.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import moment.reward.domain.Reason;
import moment.reward.infrastructure.RewardRepository;
import moment.user.domain.User;
import moment.reward.domain.PointHistory;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class PointRewardServiceTest {

    @InjectMocks
    private PointRewardService pointRewardService;

    @Mock
    private RewardRepository rewardRepository;

    @Test
    void 코멘트_작성시_5포인트를_부여한다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        int commentPointTo = reason.getPointTo();
        User commenter = new User("ekorea623@gmail.com", "1q2w3e4r!", "드라고");

        // when
        pointRewardService.reward(commenter, reason);

        // then
        assertThat(commenter.getCurrentPoint()).isEqualTo(commentPointTo);
        verify(rewardRepository).save(any(PointHistory.class));
    }

    @Test
    void 이모지_수신시_15포인트를_부여한다() {
        // given
        Reason reason = Reason.POSITIVE_EMOJI_RECEIVED;
        int positiveEmojiReceivedPointTo = reason.getPointTo();
        User commenter = new User("ekorea623@gmail.com", "1q2w3e4r!", "드라고");

        // when
        pointRewardService.reward(commenter, reason);

        // then
        assertThat(commenter.getCurrentPoint()).isEqualTo(positiveEmojiReceivedPointTo);
        verify(rewardRepository).save(any(PointHistory.class));
    }
}