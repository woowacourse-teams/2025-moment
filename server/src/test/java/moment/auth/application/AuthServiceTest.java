package moment.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import moment.auth.dto.request.LoginRequest;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
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
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenManager tokenManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void 로그인에_성공한다() {
        // given
        LoginRequest request = new LoginRequest("ekorea623@gmail.com", "1q2w3e4r");
        given(userRepository.findByEmail(any()))
                .willReturn(Optional.of(new User("ekorea623@gmail.com", "1q2w3e4r", "drago")));
        given(tokenManager.createToken(any(), any())).willReturn("asdfsvssefsdf");

        // when
        String token = authService.login(request);

        // then
        String expected = "asdfsvssefsdf";
        assertThat(token).isEqualTo(expected);
    }

    @Test
    void 로그인시_존재하지_않는_이메일을_입력한_경우_예외가_발생한다() {
        // given
        LoginRequest request = new LoginRequest("ekorea623@gmail.com", "1q2w3e4r");
        given(userRepository.findByEmail(any()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 로그인시_비밀번호를_잘못_입력한_경우_예외가_발생한다() {
        // given
        LoginRequest request = new LoginRequest("ekorea623@gmail.com", "1q2w3e4");
        given(userRepository.findByEmail(any()))
                .willReturn(Optional.of(new User("ekorea623@gmail.com", "1q2w3e4r", "drago")));

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 토큰에서_인증정보를_추출할_수_있다() {
        // given
        String token = "validToken";
        Authentication authentication = new Authentication(1L);

        given(tokenManager.extractAuthentication(token)).willReturn(authentication);
        given(userRepository.existsById(1L)).willReturn(true);

        // when & then
        assertThat(authService.getAuthenticationByToken(token)).isEqualTo(authentication);
    }

    @Test
    void 토큰에서_추출한_유저의_ID가_존재하지_않을_경우_예외가_발생한다() {
        // given
        String token = "invalidToken";
        Authentication authentication = new Authentication(1L);

        given(tokenManager.extractAuthentication(token)).willReturn(authentication);
        given(userRepository.existsById(1L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.getAuthenticationByToken(token))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
}
