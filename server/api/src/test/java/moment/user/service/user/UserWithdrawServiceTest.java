package moment.user.service.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import moment.fixture.GroupFixture;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.infrastructure.GroupRepository;
import moment.notification.infrastructure.Emitters;
import moment.notification.infrastructure.PushNotificationRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserWithdrawServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private PushNotificationRepository pushNotificationRepository;

    @Mock
    private Emitters emitters;

    @InjectMocks
    private UserWithdrawService userWithdrawService;

    @Test
    void 그룹을_소유하지_않은_유저는_탈퇴_검증에_성공한다() {
        // given
        Long userId = 1L;
        given(groupRepository.findByOwnerId(userId)).willReturn(Collections.emptyList());

        // when & then
        userWithdrawService.validateWithdrawable(userId);
    }

    @Test
    void 그룹을_소유한_유저는_탈퇴_검증에_실패한다() {
        // given
        Long userId = 1L;
        User owner = UserFixture.createUserWithId(userId);
        Group group = GroupFixture.createGroup(owner);
        given(groupRepository.findByOwnerId(userId)).willReturn(List.of(group));

        // when & then
        assertThatThrownBy(() -> userWithdrawService.validateWithdrawable(userId))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_HAS_OWNED_GROUP);
    }

    @Test
    void 탈퇴_시_유저가_소프트_삭제된다() {
        // given
        Long userId = 1L;
        User user = UserFixture.createUserWithId(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userWithdrawService.withdraw(userId);

        // then
        verify(userRepository).delete(user);
    }

    @Test
    void 존재하지_않는_유저_탈퇴_시_예외가_발생한다() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userWithdrawService.withdraw(userId))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 탈퇴_시_푸시알림_구독이_삭제된다() {
        // given
        Long userId = 1L;
        User user = UserFixture.createUserWithId(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userWithdrawService.withdraw(userId);

        // then
        verify(pushNotificationRepository).deleteAllByUserId(userId);
    }

    @Test
    void 탈퇴_시_SSE_이미터가_제거된다() {
        // given
        Long userId = 1L;
        User user = UserFixture.createUserWithId(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userWithdrawService.withdraw(userId);

        // then
        verify(emitters).remove(userId);
    }
}
