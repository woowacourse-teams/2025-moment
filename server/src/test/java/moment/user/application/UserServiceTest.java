package moment.user.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.User;
import moment.user.dto.request.UserCreateRequest;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final UserCreateRequest request = new UserCreateRequest("mimi@icloud.com", "mimi1234", "mimi1234", "미미");

    @Test
    void 유저_생성에_성공한다() {
        // given
        User expect = new User("mimi@icloud.com", "mimi1234", "미미");
        given(userRepository.save(any(User.class))).willReturn(expect);
        given(passwordEncoder.encode(expect.getPassword())).willReturn("aoijwofkdl");

        // when
        userService.addUser(request);

        // then
        then(userRepository).should(times(1)).save(any(User.class));
    }

    @Test
    void 이미_존재하는_유저일_경우_예외가_발생한다() {
        // given
        given(userRepository.existsByEmail(any(String.class))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.addUser(request))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_CONFLICT);
    }

    @Test
    void 이미_존재하는_닉네임일_경우_예외가_발생한다() {
        // given
        given(userRepository.existsByNickname(any(String.class))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.addUser(request))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NICKNAME_CONFLICT);
    }

    @Test
    void 비밀번호와_비밀번호_확인_값이_일치하지_않는_경우_예외가_발생한다() {
        // given
        UserCreateRequest request = new UserCreateRequest("mimi@icloud.com", "mimi1234", "mimi5678", "미미");

        // when & then
        assertThatThrownBy(() -> userService.addUser(request))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_MISMATCHED);
    }
}
