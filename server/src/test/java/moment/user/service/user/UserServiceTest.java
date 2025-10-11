package moment.user.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import moment.global.exception.MomentException;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.dto.request.UserCreateRequest;
import moment.user.dto.response.UserProfileResponse;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import({
        UserService.class,
})
@DisplayNameGeneration(ReplaceUnderscores.class)
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setUp() {
        new User("test@email.com", "1234qwer!@", "테스트 유저", ProviderType.GOOGLE);
        new User("test@email.com", "1234qwer!@", "테스트 유저", ProviderType.EMAIL);
    }

    @Test
    void 일반_회원가입_유저를_추가한다() {
        // given
        UserCreateRequest request = new UserCreateRequest("test@email.com", "1234qwer!@", "1234qwer!@", "테스트 유저");

        // when & then
        User savedUser = userService.addUser(request.email(), request.password(), request.rePassword(),
                request.nickname());
        User findUser = userRepository.findById(savedUser.getId()).get();
        assertThat(savedUser).isEqualTo(findUser);
    }

    @Test
    void 비밀번호와_확인용_비밀번호가_일치하지_않는_경우_유저를_추가할_수_없다() {
        // given
        UserCreateRequest request = new UserCreateRequest("test@email.com", "1234qwer!@", "4567qwer!@", "테스트 유저");

        // when & then
        assertThatThrownBy(() -> userService.addUser(request.email(), request.password(), request.rePassword(),
                request.nickname()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "비밀번호가 일치하지 않습니다.");
    }

    @Test
    void 중복된_닉네임이_존재하는_경우_일반_유저를_추가할_수_없다() {
        // given
        User user = new User("test1@email.com", "1234qwer!@", "테스트 유저", ProviderType.EMAIL);
        userRepository.save(user);

        UserCreateRequest request = new UserCreateRequest("test2@email.com", "1234qwer!@", "1234qwer!@", "테스트 유저");

        // when & then
        assertThatThrownBy(() -> userService.addUser(request.email(), request.password(), request.rePassword(),
                request.nickname()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "이미 존재하는 닉네임입니다.");
    }

    @Test
    void 이미_가입한_일반_유저를_추가하는_경우_예외가_발생한다() {
        // given
        User user = new User("test@email.com", "1234qwer!@", "먼저 가입한 유저", ProviderType.EMAIL);
        userRepository.save(user);

        UserCreateRequest request = new UserCreateRequest("test@email.com", "1234qwer!@", "1234qwer!@", "중복 이메일 유저");

        // when & then
        assertThatThrownBy(() -> userService.addUser(request.email(), request.password(), request.rePassword(),
                request.nickname()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "이미 가입된 사용자입니다.");
    }

    @Test
    void 토큰으로부터_획득한_인증정보를_이용하여_유저_프로필_정보를_조회합니다() {
        // given
        User user = new User("test@email.com", "1234qwer!@", "테스트 유저", ProviderType.EMAIL);
        User savedUser = userRepository.save(user);

        Authentication authentication = new Authentication(savedUser.getId());

        // when
        UserProfileResponse userProfile = userService.getUserProfileBy(authentication);

        // then
        assertAll(
                () -> assertThat(userProfile.nickname()).isEqualTo(savedUser.getNickname()),
                () -> assertThat(userProfile.level()).isEqualTo(savedUser.getLevel()),
                () -> assertThat(userProfile.expStar()).isEqualTo(savedUser.getExpStar()),
                () -> assertThat(userProfile.nextStepExp()).isEqualTo(savedUser.getLevel().getNextLevelRequiredStars())
        );
    }

    @Test
    void 토큰으로부터_획득한_인증정보를_이용하여_유저_프로필을_조회했을_때_유저가_존재하지_않으면_예외가_발생합니다() {
        // given
        User user = new User("test@email.com", "1234qwer!@", "테스트 유저", ProviderType.EMAIL);
        userRepository.save(user);

        Authentication authentication = new Authentication(9999L);

        // when & then
        assertThatThrownBy(() -> userService.getUserProfileBy(authentication))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "존재하지 않는 사용자입니다.");
    }

    @TestConfiguration
    static class UserServiceTestConfiguration {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new PasswordEncoder() {
                @Override
                public String encode(CharSequence rawPassword) {
                    return rawPassword.toString();
                }

                @Override
                public boolean matches(CharSequence rawPassword, String encodedPassword) {
                    return rawPassword.toString().equals(encodedPassword);
                }
            };
        }
    }
}
