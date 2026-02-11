package moment.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import moment.config.TestTags;
import moment.global.config.JpaAuditingConfig;
import moment.fixture.UserFixture;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag(TestTags.INTEGRATION)
@ActiveProfiles("test")
@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 이메일과_회원가입_방식을_기준으로_사용자_존재_여부를_확인할_수_있다() {
        // given
        ProviderType providerType = ProviderType.EMAIL;
        User savedUser = userRepository.save(UserFixture.createUser());

        // when
        boolean result = userRepository.existsByEmailAndProviderType(savedUser.getEmail(), providerType);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 같은_이메일이더라도_다른_회원가입_방식인_경우_존재_여부를_확인할_수_없다() {
        // given
        User googleUser = UserFixture.createGoogleUser();
        User savedUser = userRepository.save(googleUser);

        // when
        boolean result = userRepository.existsByEmailAndProviderType(savedUser.getEmail(), ProviderType.EMAIL);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 이메일과_회원가입_방식을_기준으로_사용자를_조회할_수_있다() {
        // given
        User user = UserFixture.createUser();
        String email = user.getEmail();
        ProviderType providerType = user.getProviderType();

        User savedUser = userRepository.save(user);

        // when
        Optional<User> findUser = userRepository.findByEmailAndProviderType(email, providerType);

        // then
        assertAll(
                () -> assertThat(findUser).isPresent(),
                () -> assertThat(savedUser).isEqualTo(user)
        );
    }

    @Test
    void 같은_이메일이더라도_다른_회원가입_방식인_경우_조회할_수_없다() {
        // given
        User googleUser = UserFixture.createGoogleUser();
        User savedUser = userRepository.save(googleUser);

        // when
        Optional<User> emailSignUpUser = userRepository.findByEmailAndProviderType(
                savedUser.getEmail(),
                ProviderType.EMAIL
        );

        // then
        assertThat(emailSignUpUser).isEmpty();
    }

    @Test
    void 닉네임을_가진_유저가_있다면_참을_반환한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        // when & then
        assertThat(userRepository.existsByNickname(savedUser.getNickname())).isTrue();
    }

    @Test
    void 닉네임을_가진_유저가_없다면_거짓을_반환한다() {
        // given
        User user = UserFixture.createUser();
        userRepository.save(user);

        String notExistedNickname = "notExisted" + user.getEmail();

        // when & then
        assertThat(userRepository.existsByNickname(notExistedNickname)).isFalse();
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
        List<User> usersByIds = userRepository.findAllByIdIn(ids);

        assertAll(
                () -> assertThat(usersByIds).hasSize(3),
                () -> assertThat(usersByIds).containsAll(users)
        );
    }
}
