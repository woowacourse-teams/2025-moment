package moment.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void 이메일을_가진_유저가_있다면_참을_반환한다() {
        // given
        String existedEmail = "mimi@icloud.com";
        userRepository.save(new User(existedEmail, "password", "mimi"));

        // when & then
        assertThat(userRepository.existsByEmail(existedEmail)).isTrue();
    }

    @Test
    void 이메일을_가진_유저가_없다면_거짓을_반환한다() {
        // given
        String notExistedEmail = "mimi@icloud.com";
        userRepository.save(new User("hippo@gmail.com", "password", "hippo"));

        // when & then
        assertThat(userRepository.existsByEmail(notExistedEmail)).isFalse();
    }

    @Test
    void 닉네임을_가진_유저가_있다면_참을_반환한다() {
        // given
        String existedNickname = "mimi";
        userRepository.save(new User("mimi@icloud.com", "password", existedNickname));

        // when & then
        assertThat(userRepository.existsByNickname(existedNickname)).isTrue();
    }

    @Test
    void 닉네임을_가진_유저가_없다면_거짓을_반환한다() {
        // given
        String notExistedNickname = "hippo";
        userRepository.save(new User("mimi@icloud.com", "password", "mimi"));

        // when & then
        assertThat(userRepository.existsByNickname(notExistedNickname)).isFalse();
    }
}
