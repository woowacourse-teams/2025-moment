package moment.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import moment.auth.application.GoogleAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Google Auth API", description = "Google 인증 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/google") // TODO : URI 어떻게 하지?
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    @Value("${auth.google.client-id}")
    private String clientId;

    @Value("${auth.google.redirect-uri}")
    private String redirectUri;

    @Operation(summary = "Google 로그인 시작", description = "사용자를 Google 로그인 페이지로 리디렉션합니다.")
    @GetMapping("/login")
    public void googleLogin(HttpServletResponse response) throws IOException {
        String scope = "email profile";
        String authUrl = UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", scope)
                .build()
                .toUriString();

        response.sendRedirect(authUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> googleCallback(@RequestParam String code) {
        String token = googleAuthService.loginOrSignUp(code);

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .sameSite("None")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(1800)
                .build();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.LOCATION, "http://localhost:3000/callback") // TODO : www.connectingmoment.com/xxx
                .build();
    }
}
