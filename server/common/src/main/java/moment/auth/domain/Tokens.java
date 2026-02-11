package moment.auth.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Tokens {

    private final String accessToken;
    private final RefreshToken refreshToken;

    public Tokens(final String accessToken, final RefreshToken refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
