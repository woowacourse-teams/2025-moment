package moment.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import moment.auth.dto.google.GoogleUserInfo;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.PendingUser;
import moment.user.infrastructure.PendingUserCache;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PendingUserCacheServiceTest {

    @InjectMocks
    private PendingUserCacheService pendingUserCacheService;

    @Mock
    private PendingUserCache pendingUserCache;

    @Test
    void 구글_유저_정보로_임시_유저를_등록한다() {
        // given
        String email = "test@moment.com";
        GoogleUserInfo googleUserInfo = new GoogleUserInfo("sub", "name", "givenName", "picture", email, true);
        ArgumentCaptor<PendingUser> pendingUserCaptor = ArgumentCaptor.forClass(PendingUser.class);

        // when
        pendingUserCacheService.register(googleUserInfo);

        // then
        verify(pendingUserCache, times(1)).save(pendingUserCaptor.capture());
        PendingUser capturedUser = pendingUserCaptor.getValue();

        assertAll(
            () -> assertThat(capturedUser.email()).isEqualTo(email),
            () -> assertThat(capturedUser.googleUserInfo()).isEqualTo(googleUserInfo)
        );
    }

    @Test
    void 임시_유저_정보를_조회한다() {
        // given
        String email = "test@moment.com";
        GoogleUserInfo googleUserInfo = new GoogleUserInfo("sub", "name", "givenName", "picture", email, true);
        PendingUser pendingUser = new PendingUser(email, googleUserInfo);

        when(pendingUserCache.getPendingUser(email)).thenReturn(pendingUser);

        // when
        PendingUser foundUser = pendingUserCacheService.getPendingUser(email);

        // then
        assertAll(
            () -> assertThat(foundUser).isNotNull(),
            () -> assertThat(foundUser.email()).isEqualTo(email)
        );
    }

    @Test
    void 등록되지_않은_임시_유저_조회시_예외가_발생한다() {
        // given
        String nonExistentEmail = "nonexistent@moment.com";
        when(pendingUserCache.getPendingUser(nonExistentEmail))
            .thenThrow(new MomentException(ErrorCode.PENDING_USER_NOT_FOUND));

        // when & then
        MomentException thrown = assertThrows(MomentException.class, () -> {
            pendingUserCacheService.getPendingUser(nonExistentEmail);
        });

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PENDING_USER_NOT_FOUND);
    }

    @Test
    void 등록된_임시_유저를_삭제한다() {
        // given
        String email = "test@moment.com";
        doNothing().when(pendingUserCache).remove(email);

        // when
        pendingUserCacheService.removePendingUser(email);

        // then
        verify(pendingUserCache, times(1)).remove(email);
    }
}