package moment.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserRepositoryTest {

    private final String email = "mimi@icloud.com";

    @Autowired
    private UserRepository userRepository;

    @Test
    void 이메일과_회원가입_방식을_기준으로_사용자_존재_여부를_확인할_수_있다() {
        // given
        ProviderType providerType = ProviderType.EMAIL;
        userRepository.save(new User(email, "password", "mimi", providerType));

        // when
        boolean result = userRepository.existsByEmailAndProviderType(email, providerType);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 같은_이메일이더라도_다른_회원가입_방식인_경우_존재_여부를_확인할_수_없다() {
        // given
        User googleUser = new User(email, "password", "mimi", ProviderType.GOOGLE);
        userRepository.save(googleUser);

        // when
        boolean result = userRepository.existsByEmailAndProviderType(email, ProviderType.EMAIL);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 이메일과_회원가입_방식을_기준으로_사용자를_조회할_수_있다() {
        // given
        String email = "mimi@icloud.com";
        ProviderType providerType = ProviderType.EMAIL;
        userRepository.save(new User(email, "password", "mimi", providerType));

        // when
        Optional<User> user = userRepository.findByEmailAndProviderType(email, providerType);

        // then
        assertAll(
                () -> assertThat(user).isPresent(),
                () -> assertThat(user.get().getEmail()).isEqualTo(email)
        );
    }

    @Test
    void 같은_이메일이더라도_다른_회원가입_방식인_경우_조회할_수_없다() {
        // given
        String email = "mimi@icloud.com";
        User googleUser = new User(email, "password", "mimi", ProviderType.GOOGLE);
        userRepository.save(googleUser);

        // when
        Optional<User> emailSignUpUser = userRepository.findByEmailAndProviderType(email, ProviderType.EMAIL);

        // then
        assertThat(emailSignUpUser).isEmpty();
    }

    @Test
    void 닉네임을_가진_유저가_있다면_참을_반환한다() {
        // given
        String existedNickname = "mimi";
        userRepository.save(new User("mimi@icloud.com", "password", existedNickname, ProviderType.EMAIL));

        // when & then
        assertThat(userRepository.existsByNickname(existedNickname)).isTrue();
    }

    @Test
    void 닉네임을_가진_유저가_없다면_거짓을_반환한다() {
        // given
        String notExistedNickname = "hippo";
        userRepository.save(new User("mimi@icloud.com", "password", "mimi", ProviderType.EMAIL));

        // when & then
        assertThat(userRepository.existsByNickname(notExistedNickname)).isFalse();
    }
}
