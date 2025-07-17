package moment.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import moment.auth.dto.LoginRequest;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
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
}