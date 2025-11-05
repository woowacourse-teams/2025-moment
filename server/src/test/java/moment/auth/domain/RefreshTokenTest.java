package moment.auth.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDateTime;
import java.util.Date;
import moment.fixture.UserFixture;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
public class RefreshTokenTest {

    @Test
    void 리프레시_토큰이_만료됐는지_확인한다() {
        // given
        User user = UserFixture.createUser();
        RefreshToken refreshToken = new RefreshToken(
                "token",
                user,
                new Date(System.currentTimeMillis() - 50),
                new Date(System.currentTimeMillis() - 100));

        // when & then
        assertThat(refreshToken.isExpired(LocalDateTime.now())).isTrue();
    }

    @Test
    void 리프레시_토큰을_갱신한다() {
        // given
        User user = UserFixture.createUser();
        RefreshToken refreshToken = new RefreshToken(
                "token",
                user,
                new Date(System.currentTimeMillis() - 100),
                new Date(System.currentTimeMillis() + 100));
        // when
        String newToken = "newToken";
        refreshToken.renew(
                newToken,
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + 10000));

        // then
        assertThat(refreshToken.getTokenValue()).isEqualTo(newToken);
    }
}
