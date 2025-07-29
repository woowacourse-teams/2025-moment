package moment.moment.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.moment.application.MomentService;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MatchedMomentResponse;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MyMomentResponse;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Moment API", description = "모멘트 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/moments")
public class MomentController {

    private final MomentService momentService;

    @Operation(summary = "모멘트 등록", description = "사용자가 모멘트를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "모멘트 등록 성공"),
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
    @PostMapping
    public ResponseEntity<SuccessResponse<MomentCreateResponse>> createMoment(
            @RequestBody MomentCreateRequest request,
            @AuthenticationPrincipal Authentication authentication
    ) {
        MomentCreateResponse response = momentService.addMomentAndMatch(request, authentication.id());
        HttpStatus status = HttpStatus.CREATED;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "내 모멘트 조회", description = "사용자가 자신의 모멘트를 최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 모멘트 조회 성공"),
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
    public ResponseEntity<SuccessResponse<List<MyMomentResponse>>> readMyMoment(
            @AuthenticationPrincipal Authentication authentication
    ) {
        List<MyMomentResponse> responses = momentService.getMyMoments(authentication.id());
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }

    @Operation(summary = "매칭된 모멘트 조회", description = "사용자에게 매칭된 모멘트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매칭된 모멘트 조회 성공"),
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
    @GetMapping("/matching")
    public ResponseEntity<SuccessResponse<MatchedMomentResponse>> readMatchedMoment(
            @AuthenticationPrincipal Authentication authentication
    ) {
        MatchedMomentResponse responses = momentService.getMatchedMoment(authentication.id());
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }

    @Operation(summary = "모멘트 작성여부 확인", description = "유저가 오늘 모멘트를 더 보낼 수 있는지 확인입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매칭된 모멘트 조회 성공"),
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
    @GetMapping("/me/creation-status")
    public ResponseEntity<SuccessResponse<MomentCreationStatusResponse>> getMomentCreationStatus(
            @AuthenticationPrincipal Authentication authentication
    ) {
        MomentCreationStatusResponse responses = momentService.canCreateMoment(authentication.id());
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }
}
