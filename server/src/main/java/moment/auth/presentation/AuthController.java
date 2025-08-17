package moment.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import moment.auth.application.AuthService;
import moment.auth.application.EmailService;
import moment.auth.application.GoogleAuthService;
import moment.auth.dto.request.EmailRequest;
import moment.auth.dto.request.EmailVerifyRequest;
import moment.auth.dto.request.LoginRequest;
import moment.auth.dto.request.PasswordResetRequest;
import moment.auth.dto.request.PasswordUpdateRequest;
import moment.auth.dto.response.LoginCheckResponse;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Auth API", description = "인증/인가 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final EmailService emailService;

    @Value("${auth.google.client-id}")
    private String clientId;

    @Value("${auth.google.redirect-uri}")
    private String redirectUri;

    @Operation(summary = "로그인", description = "사용자 로그인을 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-001] 유효하지 않은 토큰입니다.
                    - [T-002] 만료된 토큰입니다.
                    - [T-003] 빈 토큰입니다.
                    - [T-004] 서명되지 않은 토큰입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 아이디 또는 비밀번호가 일치하지 않습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<Void>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .sameSite("none")
                .secure(true)
                .httpOnly(true)
                .path("/")
                .maxAge(1800)
                .build();

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout() {
        ResponseCookie cookie = ResponseCookie.from("token", null)
                .sameSite("none")
                .secure(true)
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "Google 로그인 시작", description = "사용자를 Google 로그인 페이지로 리디렉션합니다.")
    @GetMapping("/login/google")
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

    @GetMapping("/callback/google")
    public ResponseEntity<Void> googleCallback(@RequestParam String code) {
        String token = googleAuthService.loginOrSignUp(code);

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .sameSite("None")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(1800)
                .build();

        String redirectUrl = UriComponentsBuilder.fromUriString("http://www.connectingmoment.com")
                .path("/auth/callback")
                .queryParam("success", "true")
                .build()
                .toUriString();

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    @Operation(summary = "로그인 상태 확인", description = "사용자가 로그인 상태인지 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 상태 확인 성공"),
    })
    @GetMapping("/login/check")
    public ResponseEntity<SuccessResponse<LoginCheckResponse>> checkLogin(@CookieValue(value = "token", required = false) String token) {
        LoginCheckResponse response = authService.loginCheck(token);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.ok(SuccessResponse.of(status, response));
    }

    @Operation(summary = "이메일 인증 요청", description = "사용자의 이메일 인증을 요청합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이메일 인증 코드 전송 성공"),
            @ApiResponse(responseCode = "400", description = """
            - [V-002] 이메일 요청은 1분에 한번만 요청 할 수 있습니다.
            """)
    })
    @PostMapping("/email")
    public ResponseEntity<SuccessResponse<Void>> checkEmail(@Valid @RequestBody EmailRequest request) {
        emailService.sendVerificationEmail(request);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK, null));
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "사용자가 입력한 이메일 인증 코드를 확인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
        @ApiResponse(responseCode = "400", description = """
                - [V-001] 이메일 인증에 실패했습니다.
                """,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/email/verify")
    public ResponseEntity<SuccessResponse<Void>> verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
        emailService.verifyCode(request);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK, null));
    }

    @Operation(summary = "비밀번호 변경 요청", description = "비밀번호 변경을 위한 페이지를 이메일로 요청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 링크 전송 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [V-002] 이메일 요청은 1분에 한번만 요청 할 수 있습니다.
                    - [V-003] 이메일 전송에 실패했습니다.
                    """)
    })
    @PostMapping("/email/password")
    public ResponseEntity<SuccessResponse<Void>> requestPasswordUpdatePage(
            @RequestBody PasswordUpdateRequest request
    ) {
        emailService.sendPasswordUpdateEmail(request);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK, null));
    }

    @Operation(summary = "이메일을 통한 비밀번호 재설정", description = "토큰을 검증하고 비밀번호를 재설정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
        @ApiResponse(responseCode = "400", description = """
                - [V-004] 유효하지 않은 비밀번호 재설정 요청입니다.
                - [U-007] 비밀번호가 일치하지 않습니다.
                """)
    })
    @PostMapping("/email/password/reset")
    public ResponseEntity<SuccessResponse<Void>> resetPassword(
        @Valid @RequestBody PasswordResetRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK, null));
    }
}
