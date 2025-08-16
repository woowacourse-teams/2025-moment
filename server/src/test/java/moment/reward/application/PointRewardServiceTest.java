package moment.reward.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.reward.domain.Reason;
import moment.reward.infrastructure.RewardRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.reward.domain.PointHistory;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
        User commenter = new User("ekorea623@gmail.com", "1q2w3e4r!", "드라고", ProviderType.EMAIL);
        User momenter = new User("hipo@gmail.com", "1q2w3e4r!", "히포", ProviderType.EMAIL);
        Comment comment = new Comment("정말 대단합니다!", commenter, new Moment("오늘의 달리기 성공!", momenter, WriteType.BASIC));
        ReflectionTestUtils.setField(comment, "id", 1L);

        // when
        pointRewardService.reward(commenter, reason, comment.getId());

        // then
        assertThat(commenter.getCurrentPoint()).isEqualTo(commentPointTo);
        verify(rewardRepository).save(any(PointHistory.class));
    }

    @Test
    void 이모지_수신시_15포인트를_부여한다() {
        // given
        Reason reason = Reason.POSITIVE_EMOJI_RECEIVED;
        int positiveEmojiReceivedPointTo = reason.getPointTo();
        User commenter = new User("ekorea623@gmail.com", "1q2w3e4r!", "드라고", ProviderType.EMAIL);
        User momenter = new User("hipo@gmail.com", "1q2w3e4r!", "히포", ProviderType.EMAIL);
        Comment comment = new Comment("정말 대단합니다!", commenter, new Moment("오늘의 달리기 성공!", momenter, WriteType.BASIC));
        ReflectionTestUtils.setField(comment, "id", 1L);

        // when
        pointRewardService.reward(commenter, reason, comment.getId());

        // then
        assertThat(commenter.getCurrentPoint()).isEqualTo(positiveEmojiReceivedPointTo);
        verify(rewardRepository).save(any(PointHistory.class));
    }

    @Test
    void 중복된_작업_요청시_포인트가_부여되지_않는다() {
        // given
        Reason reason = Reason.POSITIVE_EMOJI_RECEIVED;
        User momenter = new User("hipo@gmail.com", "1q2w3e4r!", "히포", ProviderType.EMAIL);
        User commenter = new User("ekorea623@gmail.com", "1q2w3e4r!", "드라고", ProviderType.EMAIL);
        Comment comment = new Comment("정말 대단합니다!", commenter, new Moment("오늘의 달리기 성공!", momenter, WriteType.BASIC));
        ReflectionTestUtils.setField(comment, "id", 1L);

        given(rewardRepository.existsByUserAndReasonAndContentId(commenter, reason, comment.getId()))
                .willReturn(true);

        // when
        pointRewardService.reward(commenter, reason, comment.getId());

        // then
        assertThat(commenter.getCurrentPoint()).isEqualTo(0);
    }
}
