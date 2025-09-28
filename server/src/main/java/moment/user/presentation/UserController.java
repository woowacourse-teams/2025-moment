package moment.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.auth.application.AuthService;
import moment.auth.domain.Tokens;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.auth.presentation.PendingAuthenticationPrincipal;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.user.application.UserService;
import moment.user.dto.request.Authentication;
import moment.user.dto.request.BasicUserCreateRequest;
import moment.user.dto.request.GoogleOAuthUserCreateRequest;
import moment.user.dto.request.NicknameConflictCheckRequest;
import moment.user.dto.response.MomentRandomNicknameResponse;
import moment.user.dto.response.NicknameConflictCheckResponse;
import moment.user.dto.response.UserProfileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "User API", description = "사용자 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private static final int ACCESS_TOKEN_TIME = 30 * 60;
    private static final int REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60;

    private static final String ACCESS_TOKEN_HEADER = "accessToken";
    private static final String REFRESH_TOKEN_HEADER = "refreshToken";
    private final AuthService authService;
    private final UserService userService;
    @Value("${auth.google.client-uri}")
    private String clientUri;

    @Operation(summary = "회원 가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [U-004] 유효하지 않은 이메일 형식입니다.
                    - [U-005] 유효하지 않은 비밀번호 형식입니다.
                    - [U-006] 유효하지 않은 닉네임 형식입니다.
                    - [U-007] 비밀번호가 일치하지 않습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "409", description = """
                    - [U-001] 이미 가입된 사용자입니다.
                    - [U-003] 이미 존재하는 닉네임입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<Void>> createUser(@Valid @RequestBody BasicUserCreateRequest request) {
        userService.registerUser(request);
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "Google OAuth 회원 가입", description = "새로운 Google OAuth 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [U-004] 유효하지 않은 이메일 형식입니다.
                    - [U-006] 유효하지 않은 닉네임 형식입니다.
                    - [U-014] 유효하지 않은 이메일 구독 설정 정보입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "409", description = """
                    - [U-001] 이미 가입된 사용자입니다.
                    - [U-003] 이미 존재하는 닉네임입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/signup/google")
    public ResponseEntity<Void> createUserByGoogleAndLogin(
            @Valid @RequestBody GoogleOAuthUserCreateRequest request,
            @PendingAuthenticationPrincipal String authenticationEmail) {

        Tokens tokens = userService.registerAndLoginGoogleOAuthUser(request, authenticationEmail);

        String accessToken = tokens.getAccessToken();
        String refreshToken = tokens.getRefreshToken().getTokenValue();

        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_HEADER, accessToken)
                .sameSite("None")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(ACCESS_TOKEN_TIME)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_HEADER, refreshToken)
                .sameSite("None")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(REFRESH_TOKEN_TIME)
                .build();

        // todo 여기서 회원가입 완료했을 때, 어디로 보내주면 되는지? /auth/callback은 이미 추가정보 기입때 한번 사용함.
        String redirectUrl = UriComponentsBuilder.fromUriString(clientUri)
                .path("/auth/callback")
                .queryParam("success", "true")
                .build()
                .toUriString();

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    @Operation(summary = "프로필 조회", description = "사용자가 프로필을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> readUserProfile(
            @AuthenticationPrincipal Authentication authentication
    ) {
        UserProfileResponse response = userService.getUserProfile(authentication);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "닉네임 중복 여부 조회", description = "닉네임 중복 여부를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 중복 여부 조회 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [U-006] 유효하지 않은 닉네임 형식입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/signup/nickname/check")
    public ResponseEntity<SuccessResponse<NicknameConflictCheckResponse>> readNicknameConflict(
            @Valid @RequestBody NicknameConflictCheckRequest request
    ) {
        NicknameConflictCheckResponse response = userService.checkNicknameConflict(request);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "랜덤 닉네임 생성하기", description = "사용 가능한 랜덤 닉네임을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "랜덤 닉네임 생성 성공"),
            @ApiResponse(responseCode = "409", description = """
                    - [U-010] 사용 가능한 닉네임을 생성할 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @GetMapping("/signup/nickname")
    public ResponseEntity<SuccessResponse<MomentRandomNicknameResponse>> readRandomNickname() {
        MomentRandomNicknameResponse response = userService.createRandomNickname();
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
