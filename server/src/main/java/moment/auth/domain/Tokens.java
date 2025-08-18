package moment.auth.domain;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Tokens {

    private final Map<String, Object> tokens = new HashMap<>();

    public Tokens(String accessToken, RefreshToken refreshToken) {
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
    }


}
