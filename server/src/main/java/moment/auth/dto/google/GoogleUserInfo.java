package moment.auth.dto.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GoogleUserInfo {
    @JsonProperty("sub")
    String sub;

    @JsonProperty("name")
    String name;

    @JsonProperty("given_name")
    String givenName;

    @JsonProperty("picture")
    String picture;

    @JsonProperty("email")
    String email;

    @JsonProperty("email_verified")
    boolean emailVerified;
}
