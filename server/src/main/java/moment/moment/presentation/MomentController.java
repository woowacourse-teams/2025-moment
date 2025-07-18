package moment.moment.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.moment.application.MomentService;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MomentCreateResponse;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    
    @Operation(summary = "모멘트 전달", description = "사용자가 모멘트를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "모멘트 등록 성공"),
            @ApiResponse(responseCode = "400", description = """
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
        MomentCreateResponse response = momentService.addMoment(request, authentication.id());
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
