package moment.auth.infrastructure;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import moment.auth.dto.google.GoogleAccessToken;
import moment.auth.dto.google.GoogleUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GoogleAuthClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${auth.google.client-id}")
    private String clientId;

    @Value("${auth.google.client-secret}")
    private String clientSecret;

    @Value("${auth.google.redirect-uri}")
    private String redirectUri;

    public GoogleAccessToken getAccessToken(String authorizationCode) {
        Map<String, String> params = new HashMap<>();
        params.put("code", authorizationCode);
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("redirect_uri", redirectUri);
        params.put("grant_type", "authorization_code");

        ResponseEntity<GoogleAccessToken> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                params,
                GoogleAccessToken.class
        );

        return response.getBody();
    }

    public GoogleUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                entity,
                GoogleUserInfo.class
        );

        return response.getBody();
    }
}
