package moment.user.presentation;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.user.application.MyPageService;
import moment.user.dto.request.Authentication;
import moment.user.dto.request.NicknameChangeRequest;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.dto.response.MyRewardHistoryPageResponse;
import moment.user.dto.response.NicknameChangeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MyPage API", description = "마이페이지 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class MyPageController {

    private final MyPageService myPageService;

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
        MyPageProfileResponse response = myPageService.getProfile(authentication.id());
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "마이페이지 나의 보상 기록 페이징 조회", description = "마이페이지에서 나의 보상 기록을 페이지 별로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "마이페이지 나의 보상 기록 조회 성공"),
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
    @GetMapping("/reward/history")
    public ResponseEntity<SuccessResponse<MyRewardHistoryPageResponse>> readMyRewardHistoryPage(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @AuthenticationPrincipal Authentication authentication
    ) {
        MyRewardHistoryPageResponse response = myPageService.getMyRewardHistory(pageNum, pageSize, authentication.id());
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }


    @Operation(summary = "마이페이지 닉네임 변경", description = "리워드를 사용하여 닉네임을 변경합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 변경 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [U-006] 유효하지 않은 닉네임 형식입니다.
                    - [U-011] 사용 가능한 별조각을 확인해주세요.
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
        NicknameChangeResponse response = myPageService.changeNickname(request, authentication.id());
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
