package moment.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.auth.application.AuthService;
import moment.auth.dto.request.LoginRequest;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth API", description = "인증/인가 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

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
}
