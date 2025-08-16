package moment.user.application;

import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.Level;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.response.MyPageProfileResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
