package moment.user.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import moment.fixture.UserFixture;
import moment.global.exception.MomentException;
import moment.reward.domain.Reason;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.dto.request.UserCreateRequest;
import moment.user.dto.response.NicknameConflictCheckResponse;
import moment.user.dto.response.UserProfileResponse;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@DisplayNameGeneration(ReplaceUnderscores.class)
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void 일반_회원가입_유저를_추가한다() {
        // given
        UserCreateRequest request = UserFixture.createUserCreateRequest();

        // when & then
        User savedUser = userService.addUser(request.email(), request.password(), request.rePassword(),
                request.nickname());
        User findUser = userRepository.findById(savedUser.getId()).get();
        assertThat(savedUser).isEqualTo(findUser);
    }

    @Test
    void 비밀번호와_확인용_비밀번호가_일치하지_않는_경우_유저를_추가할_수_없다() {
        // given
        UserCreateRequest request = UserFixture.createUserCreateRequestByPassword("1234qwer!@",
                "4567qwer!@");

        // when & then
        assertThatThrownBy(() -> userService.addUser(request.email(), request.password(), request.rePassword(),
                request.nickname()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "비밀번호가 일치하지 않습니다.");
    }

    @Test
    void 중복된_닉네임이_존재하는_경우_일반_유저를_추가할_수_없다() {
        // given
        User user = UserFixture.createUser();
        userRepository.save(user);

        UserCreateRequest request = UserFixture.createUserCreateRequestByNickname(user.getNickname());

        // when & then
        assertThatThrownBy(() -> userService.addUser(request.email(), request.password(), request.rePassword(),
                request.nickname()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "이미 존재하는 닉네임입니다.");
    }

    @Test
    void 이미_가입한_일반_유저를_추가하는_경우_예외가_발생한다() {
        // given
        User user = UserFixture.createUser();
        userRepository.save(user);

        UserCreateRequest request = UserFixture.createUserCreateRequestByEmail(user.getEmail());

        // when & then
        assertThatThrownBy(() -> userService.addUser(request.email(), request.password(), request.rePassword(),
                request.nickname()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "이미 가입된 사용자입니다.");
    }

    @Test
    void 토큰으로부터_획득한_인증정보를_이용하여_유저_프로필_정보를_조회합니다() {
        // given
        User user = UserFixture.createUser();
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
        User user = UserFixture.createUser();
        userRepository.save(user);

        Authentication authentication = new Authentication(9999L);

        // when & then
        assertThatThrownBy(() -> userService.getUserProfileBy(authentication))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "존재하지 않는 사용자입니다.");
    }

    @Test
    void 중복된_닉네임을_사용중인_유저가_있다면_isExists_true를_반환합니다() {
        // given
        User user = UserFixture.createUser();
        userRepository.save(user);

        // when
        String nickname = user.getNickname();
        NicknameConflictCheckResponse nicknameConflictCheckResponse = userService.checkNicknameConflict(nickname);

        // then
        assertThat(nicknameConflictCheckResponse.isExists()).isTrue();
    }

    @Test
    void 중복된_닉네임을_사용중인_유저가_없다면_isExists_false를_반환합니다() {
        // given
        User user = UserFixture.createUser();
        userRepository.save(user);

        // when
        String nickname = "새로운 유저";
        NicknameConflictCheckResponse nicknameConflictCheckResponse = userService.checkNicknameConflict(nickname);

        // then
        assertThat(nicknameConflictCheckResponse.isExists()).isFalse();
    }

    @Test
    void ID를_이용하여_유저를_조회합니다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        // when & then
        assertThat(userService.getUserBy(savedUser.getId())).isEqualTo(savedUser);
    }

    @Test
    void ID를_이용하여_유저를_조회하는_경우_존재하지_않으면_예외가_발생한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        // when & then
        assertThatThrownBy(() -> userService.getUserBy(999L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "존재하지 않는 사용자입니다.");
    }

    @Test
    void 이메일과_가입_유형으로_유저를_조회합니다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        // when
        Optional<User> result = userService.findUserBy(savedUser.getEmail(), ProviderType.EMAIL);

        // then
        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get()).isEqualTo(savedUser)
        );
    }

    @ParameterizedTest
    @CsvSource(value = {"other@email.com,EMAIL", "test@email.com,GOOGLE"})
    void 이메일과_가입_유형으로_유저를_조회하는_경우_존재하지_않으면_빈_Optional을_반환한다(String email, ProviderType providerType) {
        // given
        User user = UserFixture.createUser();
        userRepository.save(user);

        // when
        Optional<User> result = userService.findUserBy(email, providerType);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void ID_목록으로_유저_목록을_조회한다() {
        // given
        int amount = 3;
        List<User> users = UserFixture.createUsersByAmount(amount);

        List<Long> ids = new ArrayList<>();
        for (User user : users) {
            User savedUser = userRepository.save(user);
            ids.add(savedUser.getId());
        }

        // when
        List<User> usersByIds = userService.getAllBy(ids);

        assertAll(
                () -> assertThat(usersByIds).hasSize(3),
                () -> assertThat(usersByIds).containsAll(users)
        );
    }

    @Test
    void ID_목록에_해당하는_유저가_없는_경우_빈_목록을_반환한다() {
        // given
        int amount = 3;
        List<User> users = UserFixture.createUsersByAmount(amount);
        for (User user : users) {
            userRepository.save(user);
        }

        List<Long> ids = List.of(999L, 1000L, 1001L);

        // when
        List<User> usersByIds = userService.getAllBy(ids);

        // then
        assertThat(usersByIds).isEmpty();
    }

    @Test
    void 닉네임을_사용하는_유저가_존재하는_경우_true_반환한다() {
        // given
        String nickname = "테스트 유저";
        User user = UserFixture.createUserByNickname(nickname);
        userRepository.save(user);

        // when & then
        assertThat(userService.existsBy(nickname)).isTrue();
    }

    @Test
    void 닉네임을_사용하는_유저가_존재하지_않는_경우_true_반환한다() {
        // given
        User user = UserFixture.createUser();
        userRepository.save(user);

        // when & then
        assertThat(userService.existsBy("새로운 유저")).isFalse();
    }

    @Test
    void 리워드를_소모하여_유저_닉네임을_변경합니다() throws NoSuchFieldException, IllegalAccessException {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        int availableStar = 1000;
        Field field = User.class.getDeclaredField("availableStar");
        field.setAccessible(true);
        field.set(savedUser, availableStar);

        // when
        String changedNickname = "변경된 닉네임";
        userService.changeNickname(changedNickname, savedUser.getId());

        // then
        User changedUser = userRepository.findById(savedUser.getId()).get();
        assertAll(
                () -> assertThat(changedUser.getNickname()).isEqualTo(changedNickname),
                () -> assertThat(changedUser.getAvailableStar()).isEqualTo(
                        availableStar + Reason.NICKNAME_CHANGE.getPointTo())
        );
    }

    @Test
    void 사용중인_닉네임으로_변경_시_예외가_발생합니다() throws NoSuchFieldException, IllegalAccessException {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        int availableStar = 1000;
        Field field = User.class.getDeclaredField("availableStar");
        field.setAccessible(true);
        field.set(savedUser, availableStar);

        // when & then
        assertThatThrownBy(() -> userService.changeNickname(savedUser.getNickname(), savedUser.getId()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "이미 존재하는 닉네임입니다.");
    }

    @Test
    void 닉네임_변경_시_사용_가능한_리워드가_부족한_경우_예외가_발생합니다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        // when & then
        String changedNickname = "변경된 닉네임";
        assertThatThrownBy(() -> userService.changeNickname(changedNickname, savedUser.getId()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "사용 가능한 별조각을 확인해주세요.");
    }

    @Test
    void 유저_비밀번호를_변경합니다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        // when
        String newPassword = "changed123!@#";
        userService.changePassword(newPassword, newPassword, savedUser.getId());

        //
        User changedUser = userRepository.findById(savedUser.getId()).get();
        assertThat(changedUser.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void 일반_회원이_아닌_경우_비밀번호_변경_시_예외가_발생합니다() {
        // given
        User user = UserFixture.createGoogleUser();
        User savedUser = userRepository.save(user);

        // when & then
        String newPassword = "changed123!@#";
        assertThatThrownBy(() -> userService.changePassword(newPassword, newPassword, savedUser.getId()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "일반 회원가입 사용자가 아닌 경우 비밀번호를 변경할 수 없습니다.");
    }

    @Test
    void 새_비밀번호와_확인용_비밀번호가_다른_경우_예외가_발생한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        // when & then
        String newPassword = "changed123!@#";
        String checkedPassword = "checked123!@#";
        assertThatThrownBy(() -> userService.changePassword(newPassword, checkedPassword, savedUser.getId()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "비밀번호가 일치하지 않습니다.");
    }

    @Test
    void 새_비밀번호가_이전_비밀번호와_같은_경우_예외가_발생한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        // when & then
        assertThatThrownBy(() -> userService.changePassword(user.getPassword(), user.getPassword(), savedUser.getId()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "새 비밀번호가 기존의 비밀번호와 동일합니다.");
    }

    @TestConfiguration
    static class UserServiceTestConfiguration {

        @Bean
        @Primary
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
