package moment.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Optional;
import moment.auth.domain.RefreshToken;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
public class RefreshTokenRepositoryTest {

    private final String tokenValue = "token";
    private RefreshToken refreshToken;
    private User user;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        user = new User("ekorea623@gmail.com", "moment1234!", "drago", ProviderType.EMAIL);
        refreshToken = new RefreshToken(
                tokenValue,
                user,
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + 100));
        userRepository.save(user);
        refreshTokenRepository.save(refreshToken);
    }

    @Test
    void 유저로_리프레시_토큰이_존재하는지_확인한다() {
        assertThat(refreshTokenRepository.existsByUser(user)).isTrue();
    }

    @Test
    void 유저로_리프레시_토큰을_삭제한다() {
        // when
        refreshTokenRepository.deleteByUser(user);

        // then
        assertThat(refreshTokenRepository.existsByUser(user)).isFalse();
    }

    @Test
    void 토큰값으로_리프레시_토큰을_찾는다() {
        // when
        Optional<RefreshToken> token = refreshTokenRepository.findByTokenValue(tokenValue);

        // then
        assertThat(token).isPresent();
    }
}
