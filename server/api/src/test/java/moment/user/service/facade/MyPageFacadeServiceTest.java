package moment.user.service.facade;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import moment.auth.service.auth.AuthService;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.service.user.UserWithdrawService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MyPageFacadeServiceTest {

    @Mock
    private UserWithdrawService userWithdrawService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private MyPageFacadeService myPageFacadeService;

    @Test
    void 회원탈퇴_시_검증_로그아웃_탈퇴_순서로_호출된다() {
        // given
        Long userId = 1L;

        // when
        myPageFacadeService.withdraw(userId);

        // then
        InOrder inOrder = inOrder(userWithdrawService, authService);
        inOrder.verify(userWithdrawService).validateWithdrawable(userId);
        inOrder.verify(authService).logout(userId);
        inOrder.verify(userWithdrawService).withdraw(userId);
    }

    @Test
    void 그룹_소유자가_회원탈퇴를_시도하면_예외가_발생한다() {
        // given
        Long userId = 1L;
        willThrow(new MomentException(ErrorCode.USER_HAS_OWNED_GROUP))
                .given(userWithdrawService).validateWithdrawable(userId);

        // when & then
        assertThatThrownBy(() -> myPageFacadeService.withdraw(userId))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_HAS_OWNED_GROUP);

        verify(authService, never()).logout(userId);
        verify(userWithdrawService, never()).withdraw(userId);
    }
}
