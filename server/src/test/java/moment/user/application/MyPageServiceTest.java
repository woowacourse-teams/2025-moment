package moment.user.application;

import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.reward.application.RewardService;
import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.user.domain.Level;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.dto.response.MyRewardHistoryPageResponse;
import moment.user.dto.response.MyRewardHistoryResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MyPageServiceTest {

    @InjectMocks
    private MyPageService myPageService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private RewardService rewardService;

    @Test
    void 유저_프로필_정보를_조회한다() {
        // given
        User user = new User("test@gmail.com", "qwer1234!", "신비로운 행성의 지구", ProviderType.EMAIL);

        given(userQueryService.getUserById(any(Long.class))).willReturn(user);

        // when
        MyPageProfileResponse profile = myPageService.getProfile(1L);

        // then
        assertAll(
                () -> assertThat(profile.nickname()).isEqualTo(user.getNickname()),
                () -> assertThat(profile.email()).isEqualTo(user.getEmail()),
                () -> assertThat(profile.level()).isEqualTo(Level.ASTEROID_WHITE),
                () -> assertThat(profile.availableStar()).isEqualTo(0),
                () -> assertThat(profile.expStar()).isEqualTo(0),
                () -> assertThat(profile.nextStepExp()).isEqualTo(5)
        );
    }

    @Test
    void 유저가_존재하지_않는_경우_프로필_조회에_실패한다() {
        // given
        User user = new User("test@gmail.com", "qwer1234!", "신비로운 행성의 지구", ProviderType.EMAIL);

        given(userQueryService.getUserById(any(Long.class))).willThrow(new MomentException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> myPageService.getProfile(1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 유저의_보상_기록을_조회한다() {
        // given
        User user = new User("test@gmail.com", "qwer1234!", "신비로운 행성의 지구", ProviderType.EMAIL);
        ReflectionTestUtils.setField(user, "id", 1L);
        List<MyRewardHistoryResponse> content = createTestRewardHistory(user, 10);
        Pageable pageable = PageRequest.of(0, 10);

        Page<MyRewardHistoryResponse> page = new PageImpl<>(content, pageable, content.size());

        given(userQueryService.getUserById(any(Long.class))).willReturn(user);
        given(rewardService.getRewardHistoryByUser(any(User.class), any(Integer.class), any(Integer.class)))
                .willReturn(MyRewardHistoryPageResponse.from(page));

        MyRewardHistoryPageResponse response = myPageService.getMyRewardHistory(0, 10, user.getId());

        // when & then
        assertAll(
                () -> assertThat(response.totalPages()).isEqualTo(1),
                () -> assertThat(response.pageSize()).isEqualTo(10),
                () -> assertThat(response.items().size()).isEqualTo(10),
                () -> assertThat(response.currentPageNum()).isEqualTo(0)
        );
    }

    private List<MyRewardHistoryResponse> createTestRewardHistory(User user, int amount) {
        List<RewardHistory> list = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            RewardHistory rewardHistory = new RewardHistory(user, Reason.MOMENT_CREATION.getPointTo(), Reason.MOMENT_CREATION, (long) i);
            list.add(rewardHistory);
        }
        return list.stream().map(MyRewardHistoryResponse::from).toList();
    }
}
