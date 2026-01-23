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
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.user.dto.request.Authentication;
import moment.user.dto.request.ChangePasswordRequest;
import moment.user.dto.request.NicknameChangeRequest;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.dto.response.NicknameChangeResponse;
import moment.user.service.facade.MyPageFacadeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MyPage API", description = "마이페이지 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class MyPageController {

    private final MyPageFacadeService myPageFacadeService;
    private final AuthService authService;

    @Operation(summary = "마이페이지 프로필 조회", description = "마이페이지 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "마이페이지 프로필 조회 성공"),
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
    @GetMapping("/profile")
    public ResponseEntity<SuccessResponse<MyPageProfileResponse>> readProfile(
            @AuthenticationPrincipal Authentication authentication
    ) {
        MyPageProfileResponse response = myPageFacadeService.getMyProfile(authentication.id());
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "마이페이지 닉네임 변경", description = "닉네임을 변경합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 변경 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [U-006] 유효하지 않은 닉네임 형식입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = """
                    - [U-003] 이미 존재하는 닉네임입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/nickname")
    public ResponseEntity<SuccessResponse<NicknameChangeResponse>> changeNickname(
            @Valid @RequestBody NicknameChangeRequest request,
            @AuthenticationPrincipal Authentication authentication
    ) {
        NicknameChangeResponse response = myPageFacadeService.changeNickname(request, authentication.id());
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "비밀번호 변경", description = "사용자가 마이페이지 내에서 비밀번호를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [U-005] 유효하지 않은 비밀번호 형식입니다.
                    - [U-012] 새 비밀번호가 기존의 비밀번호와 동일합니다.
                    - [U-013] 일반 회원가입 사용자가 아닌 경우 비밀번호를 변경할 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/password")
    public ResponseEntity<SuccessResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal Authentication authentication
    ) {
        myPageFacadeService.changePassword(request, authentication.id());

        authService.logout(authentication.id());

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", null)
                .sameSite("none")
                .secure(true)
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", null)
                .sameSite("none")
                .secure(true)
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(SuccessResponse.of(status, null));
    }
}
