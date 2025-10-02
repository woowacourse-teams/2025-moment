package moment.reward.application;

import moment.comment.domain.Comment;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.comment.domain.Echo;
import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.reward.infrastructure.RewardRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.response.MyRewardHistoryPageResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class StarRewardServiceTest {

    @InjectMocks
    private StarRewardService starRewardService;

    @Mock
    private RewardRepository rewardRepository;

    @Test
    void 모멘트_작성시_5포인트를_부여한다() {
        // given
        Reason reason = Reason.MOMENT_CREATION;
        int momentPointTo = reason.getPointTo();
        User momenter = new User("hipo@gmail.com", "1q2w3e4r!", "히포", ProviderType.EMAIL);
        Moment moment = new Moment("테스트가 통과했으면 좋겠다!", momenter, WriteType.BASIC);
        ReflectionTestUtils.setField(moment, "id", 1L);

        // when
        starRewardService.rewardForMoment(momenter, reason, moment.getId());

        // then
        assertThat(momenter.getAvailableStar()).isEqualTo(momentPointTo);
        verify(rewardRepository).save(any(RewardHistory.class));
    }

    @Test
    void 코멘트_작성시_2포인트를_부여한다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        int commentPointTo = reason.getPointTo();
        User commenter = new User("ekorea623@gmail.com", "1q2w3e4r!", "드라고", ProviderType.EMAIL);
        User momenter = new User("hipo@gmail.com", "1q2w3e4r!", "히포", ProviderType.EMAIL);
        Comment comment = new Comment("정말 대단합니다!", commenter, new Moment("오늘의 달리기 성공!", momenter, WriteType.BASIC));
        ReflectionTestUtils.setField(comment, "id", 1L);

        // when
        starRewardService.rewardForComment(commenter, reason, comment.getId());

        // then
        assertThat(commenter.getAvailableStar()).isEqualTo(commentPointTo);
        verify(rewardRepository).save(any(RewardHistory.class));
    }

    @Test
    void 에코_수신시_3포인트를_부여한다() {
        // given
        Reason reason = Reason.ECHO_RECEIVED;
        int positiveEmojiReceivedPointTo = reason.getPointTo();
        User commenter = new User("ekorea623@gmail.com", "1q2w3e4r!", "드라고", ProviderType.EMAIL);
        User momenter = new User("hipo@gmail.com", "1q2w3e4r!", "히포", ProviderType.EMAIL);
        Comment comment = new Comment("정말 대단합니다!", commenter, new Moment("오늘의 달리기 성공!", momenter, WriteType.BASIC));
        ReflectionTestUtils.setField(comment, "id", 1L);

        // when
        starRewardService.rewardForEcho(commenter, reason, comment.getId());

        // then
        assertThat(commenter.getAvailableStar()).isEqualTo(positiveEmojiReceivedPointTo);
        verify(rewardRepository).save(any(RewardHistory.class));
    }

    @Test
    void 모멘트_작성_시_별조각은_하루_한번만_적립_가능하다() {
        // given
        Reason reason = Reason.MOMENT_CREATION;
        User momenter = new User("hipo@gmail.com", "1q2w3e4r!", "히포", ProviderType.EMAIL);
        Moment moment = new Moment("테스트가 통과했으면 좋겠다!", momenter, WriteType.BASIC);
        ReflectionTestUtils.setField(moment, "id", 1L);

        given(rewardRepository.existsByUserAndReasonAndToday(
                eq(momenter),
                eq(Reason.MOMENT_CREATION),
                any(LocalDateTime.class),
                any(LocalDateTime.class))).willReturn(true);

        // when
        starRewardService.rewardForMoment(momenter, reason, moment.getId());

        // then
        assertThat(momenter.getAvailableStar()).isEqualTo(0);
    }

    @Test
    void 중복된_코멘트_보상_지급_요청시_포인트가_부여되지_않는다() {
        // given
        Reason reason = Reason.COMMENT_CREATION;
        User momenter = new User("hipo@gmail.com", "1q2w3e4r!", "히포", ProviderType.EMAIL);
        User commenter = new User("ekorea623@gmail.com", "1q2w3e4r!", "드라고", ProviderType.EMAIL);
        Comment comment = new Comment("정말 대단합니다!", commenter, new Moment("오늘의 달리기 성공!", momenter, WriteType.BASIC));
        ReflectionTestUtils.setField(comment, "id", 1L);

        given(rewardRepository.existsByUserAndReasonAndContentId(commenter, reason, comment.getId()))
                .willReturn(true);

        // when
        starRewardService.rewardForComment(commenter, reason, comment.getId());

        // then
        assertThat(commenter.getAvailableStar()).isEqualTo(0);
    }

    @Test
    void 중복된_에코_보상_지급_요청시_포인트가_부여되지_않는다() {
        // given
        Reason reason = Reason.ECHO_RECEIVED;
        User momenter = new User("hipo@gmail.com", "1q2w3e4r!", "히포", ProviderType.EMAIL);
        User commenter = new User("ekorea623@gmail.com", "1q2w3e4r!", "드라고", ProviderType.EMAIL);
        Comment comment = new Comment("정말 대단합니다!", commenter, new Moment("오늘의 달리기 성공!", momenter, WriteType.BASIC));
        ReflectionTestUtils.setField(comment, "id", 1L);
        Echo echo = new Echo("HEART", momenter, comment);
        ReflectionTestUtils.setField(echo, "id", 1L);

        given(rewardRepository.existsByUserAndReasonAndContentId(commenter, reason, comment.getId()))
                .willReturn(true);

        // when
        starRewardService.rewardForEcho(commenter, reason, comment.getId());

        // then
        assertThat(commenter.getAvailableStar()).isEqualTo(0);
    }

    @Test
    void 유저가_보유한_별조각보다_많은_별조각을_소비하는_경우_예외가_발생한다() {
        // given
        Reason reason = Reason.NICKNAME_CHANGE;
        User user = new User("hipo@gmail.com", "1q2w3e4r!", "히포", ProviderType.EMAIL);
        ReflectionTestUtils.setField(user, "id", 1L);

        // when & then
        assertThatThrownBy(() -> starRewardService.useReward(user, reason, user.getId()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_ENOUGH_STAR);
    }

    @Test
    void 유저의_보상_기록을_조회한다() {
        // given
        User user = new User("test@gmail.com", "qwer1234!", "신비로운 행성의 지구", ProviderType.EMAIL);
        ReflectionTestUtils.setField(user, "id", 1L);
        List<RewardHistory> content = createTestRewardHistory(user, 10);
        Pageable pageable = PageRequest.of(0, 10);

        Page<RewardHistory> page = new PageImpl<>(content, pageable, content.size());

        given(rewardRepository.findByUserOrderByCreatedAtDesc(any(User.class), any(Pageable.class)))
                .willReturn(page);


        MyRewardHistoryPageResponse response = starRewardService.getRewardHistoryByUser(user, 0, 10);

        // when & then
        assertAll(
                () -> assertThat(response.totalPages()).isEqualTo(1),
                () -> assertThat(response.pageSize()).isEqualTo(10),
                () -> assertThat(response.items().size()).isEqualTo(10),
                () -> assertThat(response.currentPageNum()).isEqualTo(0)
        );
    }

    private List<RewardHistory> createTestRewardHistory(User user, int amount) {
        List<RewardHistory> list = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            RewardHistory rewardHistory = new RewardHistory(user, Reason.MOMENT_CREATION.getPointTo(), Reason.MOMENT_CREATION, (long) i);
            list.add(rewardHistory);
        }
        return list;
    }
}
