package moment.reward.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
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
        Comment comment = new Comment("정말 대단합니다!", commenter, new Moment("오늘의 달리기 성공!", momenter));
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
        Comment comment = new Comment("정말 대단합니다!", commenter, new Moment("오늘의 달리기 성공!", momenter));
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
        Comment comment = new Comment("정말 대단합니다!", commenter, new Moment("오늘의 달리기 성공!", momenter));
        ReflectionTestUtils.setField(comment, "id", 1L);

        given(rewardRepository.existsByUserAndReasonAndContentId(commenter, reason, comment.getId()))
                .willReturn(true);

        // when
        pointRewardService.reward(commenter, reason, comment.getId());

        // then
        assertThat(commenter.getCurrentPoint()).isEqualTo(0);
    }

    @Test
    void 네거티브_이모지_취소시_마이너스_15포인트를_부여한다() {
        // given
        Reason reason = Reason.CANCEL_POSITIVE_EMOJI_RECEIVED;
        int cancelEmojiPointTo = reason.getPointTo();
        User user = new User("cancel@gmail.com", "1q2w3e4r!", "취소유저", ProviderType.EMAIL);
        ReflectionTestUtils.setField(user, "currentPoint", 20); // 기존 포인트 20
        Long contentId = 1L;

        // when
        pointRewardService.reward(user, reason, contentId);

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(20 + cancelEmojiPointTo); // 20 + (-15) = 5
        verify(rewardRepository).save(any(PointHistory.class));
    }

    @Test
    void 다양한_제공자_타입_사용자에게_포인트_부여가_정상_작동한다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        int commentPointTo = reason.getPointTo();
        User googleUser = new User("google@gmail.com", "token123", "구글사용자", ProviderType.GOOGLE);
        User kakaoUser = new User("kakao@kakao.com", "token456", "카카오사용자", ProviderType.KAKAO);
        Long contentId = 1L;

        // when
        pointRewardService.reward(googleUser, reason, contentId);
        pointRewardService.reward(kakaoUser, reason, contentId);

        // then
        assertThat(googleUser.getCurrentPoint()).isEqualTo(commentPointTo);
        assertThat(kakaoUser.getCurrentPoint()).isEqualTo(commentPointTo);
        verify(rewardRepository, org.mockito.Mockito.times(2)).save(any(PointHistory.class));
    }

    @Test
    void 이미_포인트가_있는_사용자에게_추가_포인트_부여시_누적된다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        int commentPointTo = reason.getPointTo();
        User user = new User("accumulate@gmail.com", "1q2w3e4r!", "누적사용자", ProviderType.EMAIL);
        ReflectionTestUtils.setField(user, "currentPoint", 50); // 기존 포인트 50
        Long contentId = 1L;

        // when
        pointRewardService.reward(user, reason, contentId);

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(50 + commentPointTo);
        verify(rewardRepository).save(any(PointHistory.class));
    }

    @Test
    void 동일_사용자가_다른_컨텐츠에_대해_포인트_부여_요청시_정상_처리된다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        int commentPointTo = reason.getPointTo();
        User user = new User("different@gmail.com", "1q2w3e4r!", "다른컨텐츠", ProviderType.EMAIL);
        Long contentId1 = 1L;
        Long contentId2 = 2L;

        // when
        pointRewardService.reward(user, reason, contentId1);
        pointRewardService.reward(user, reason, contentId2);

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(commentPointTo * 2);
        verify(rewardRepository, org.mockito.Mockito.times(2)).save(any(PointHistory.class));
    }

    @Test
    void 동일_사용자가_같은_컨텐츠에_대해_다른_이유로_포인트_부여_요청시_정상_처리된다() {
        // given
        Reason commentReason = Reason.COMMENT_CREATION;
        Reason emojiReason = Reason.POSITIVE_EMOJI_RECEIVED;
        int totalExpectedPoints = commentReason.getPointTo() + emojiReason.getPointTo();
        User user = new User("mixed@gmail.com", "1q2w3e4r!", "혼합사용자", ProviderType.EMAIL);
        Long contentId = 1L;

        // when
        pointRewardService.reward(user, commentReason, contentId);
        pointRewardService.reward(user, emojiReason, contentId);

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(totalExpectedPoints);
        verify(rewardRepository, org.mockito.Mockito.times(2)).save(any(PointHistory.class));
    }

    @Test
    void 레포지토리_저장시_예외_발생해도_사용자_포인트는_업데이트된다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        int commentPointTo = reason.getPointTo();
        User user = new User("exception@gmail.com", "1q2w3e4r!", "예외사용자", ProviderType.EMAIL);
        Long contentId = 1L;
        
        given(rewardRepository.save(any(PointHistory.class)))
                .willThrow(new RuntimeException("Database error"));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            pointRewardService.reward(user, reason, contentId);
        });
        
        // 포인트는 여전히 업데이트되어야 함
        assertThat(user.getCurrentPoint()).isEqualTo(commentPointTo);
    }

    @Test
    void 모든_이유별_포인트_값이_올바르게_부여되는지_확인한다() {
        // given
        User user1 = new User("reason1@gmail.com", "1q2w3e4r!", "이유1", ProviderType.EMAIL);
        User user2 = new User("reason2@gmail.com", "1q2w3e4r!", "이유2", ProviderType.EMAIL);
        User user3 = new User("reason3@gmail.com", "1q2w3e4r!", "이유3", ProviderType.EMAIL);
        Long contentId = 1L;

        // when
        pointRewardService.reward(user1, Reason.COMMENT_CREATION, contentId);
        pointRewardService.reward(user2, Reason.POSITIVE_EMOJI_RECEIVED, contentId);
        pointRewardService.reward(user3, Reason.CANCEL_POSITIVE_EMOJI_RECEIVED, contentId);

        // then
        assertThat(user1.getCurrentPoint()).isEqualTo(5);   // COMMENT_CREATION
        assertThat(user2.getCurrentPoint()).isEqualTo(15);  // POSITIVE_EMOJI_RECEIVED
        assertThat(user3.getCurrentPoint()).isEqualTo(-15); // CANCEL_POSITIVE_EMOJI_RECEIVED
        verify(rewardRepository, org.mockito.Mockito.times(3)).save(any(PointHistory.class));
    }

    @Test
    void 중복_체크에서_예외_발생시에도_적절히_처리된다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        User user = new User("duplicate@gmail.com", "1q2w3e4r!", "중복사용자", ProviderType.EMAIL);
        Long contentId = 1L;
        
        given(rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId))
                .willThrow(new RuntimeException("Database connection error"));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            pointRewardService.reward(user, reason, contentId);
        });
    }

    @Test
    void 경계값_컨텐츠_아이디로_테스트() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        int commentPointTo = reason.getPointTo();
        User user = new User("boundary@gmail.com", "1q2w3e4r!", "경계값", ProviderType.EMAIL);
        Long maxContentId = Long.MAX_VALUE;
        Long minContentId = 1L;

        // when
        pointRewardService.reward(user, reason, maxContentId);
        pointRewardService.reward(user, reason, minContentId);

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(commentPointTo * 2);
        verify(rewardRepository, org.mockito.Mockito.times(2)).save(any(PointHistory.class));
    }

    @Test
    void PointHistory_객체가_올바른_값으로_생성되어_저장된다() {
        // given
        Reason reason = Reason.POSITIVE_EMOJI_RECEIVED;
        User user = new User("history@gmail.com", "1q2w3e4r!", "히스토리", ProviderType.EMAIL);
        Long contentId = 123L;
        
        org.mockito.ArgumentCaptor<PointHistory> historyCaptor = org.mockito.ArgumentCaptor.forClass(PointHistory.class);

        // when
        pointRewardService.reward(user, reason, contentId);

        // then
        verify(rewardRepository).save(historyCaptor.capture());
        PointHistory capturedHistory = historyCaptor.getValue();
        assertThat(capturedHistory.getUser()).isEqualTo(user);
        assertThat(capturedHistory.getAmount()).isEqualTo(reason.getPointTo());
        assertThat(capturedHistory.getReason()).isEqualTo(reason);
        assertThat(capturedHistory.getContentId()).isEqualTo(contentId);
    }

    @Test
    void 중복_요청시_포인트_부여_및_히스토리_저장이_모두_건너뛰어진다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        User user = new User("skip@gmail.com", "1q2w3e4r!", "스킵", ProviderType.EMAIL);
        Long contentId = 1L;
        
        given(rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId))
                .willReturn(true);

        // when
        pointRewardService.reward(user, reason, contentId);

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(0);
        verify(rewardRepository, org.mockito.Mockito.never()).save(any(PointHistory.class));
    }

    @Test
    void 연속된_포인트_부여_요청이_정상_처리된다() {
        // given
        User user = new User("sequence@gmail.com", "1q2w3e4r!", "연속", ProviderType.EMAIL);
        Long contentId1 = 1L;
        Long contentId2 = 2L;
        Long contentId3 = 3L;

        // when
        pointRewardService.reward(user, Reason.COMMENT_CREATION, contentId1);
        pointRewardService.reward(user, Reason.POSITIVE_EMOJI_RECEIVED, contentId2);
        pointRewardService.reward(user, Reason.CANCEL_POSITIVE_EMOJI_RECEIVED, contentId3);

        // then
        int expectedTotal = 5 + 15 + (-15); // 5
        assertThat(user.getCurrentPoint()).isEqualTo(expectedTotal);
        verify(rewardRepository, org.mockito.Mockito.times(3)).save(any(PointHistory.class));
    }

    @Test
    void 포인트가_0이_되는_경우도_정상_처리된다() {
        // given
        Reason positiveReason = Reason.POSITIVE_EMOJI_RECEIVED;
        Reason negativeReason = Reason.CANCEL_POSITIVE_EMOJI_RECEIVED;
        User user = new User("zero@gmail.com", "1q2w3e4r!", "제로", ProviderType.EMAIL);
        Long contentId1 = 1L;
        Long contentId2 = 2L;

        // when
        pointRewardService.reward(user, positiveReason, contentId1); // +15
        pointRewardService.reward(user, negativeReason, contentId2);  // -15

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(0);
        verify(rewardRepository, org.mockito.Mockito.times(2)).save(any(PointHistory.class));
    }

    @Test
    void 대량의_포인트_요청도_정상_처리된다() {
        // given
        User user = new User("bulk@gmail.com", "1q2w3e4r!", "대량", ProviderType.EMAIL);
        Reason reason = Reason.COMMENT_CREATION;
        int requestCount = 100;

        // when
        for (int i = 1; i <= requestCount; i++) {
            pointRewardService.reward(user, reason, (long) i);
        }

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(reason.getPointTo() * requestCount);
        verify(rewardRepository, org.mockito.Mockito.times(requestCount)).save(any(PointHistory.class));
    }
}
