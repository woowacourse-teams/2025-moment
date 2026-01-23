package moment.report.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.domain.TargetType;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.report.application.facade.ReportCreateFacadeService;
import moment.report.dto.ReportCreateRequest;
import moment.report.dto.ReportCreateResponse;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report API", description = "신고 관련 API 명세")
@RestController
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportCreateFacadeService reportCreateFacadeService;

    @PostMapping("/api/v2/moments/{id}/reports")
    public ResponseEntity<SuccessResponse<ReportCreateResponse>> createMomentReport(
            @AuthenticationPrincipal Authentication authentication,
            @RequestBody ReportCreateRequest request,
            @PathVariable(name = "id") Long momentId
    ) {
        ReportCreateResponse response = reportCreateFacadeService.createReport(
                momentId, 
                authentication.id(), 
                request,
                TargetType.MOMENT);
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "코멘트 신고", description = "부적절한 코멘트를 신고합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "코멘트 신고 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    - [C-002] 존재하지 않는 코멘트입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/api/v2/comments/{id}/reports")
    public ResponseEntity<SuccessResponse<ReportCreateResponse>> createCommentReport(
            @AuthenticationPrincipal Authentication authentication,
            @RequestBody ReportCreateRequest request,
            @PathVariable(name = "id") Long commentId
    ) {
        ReportCreateResponse response = reportCreateFacadeService.createReport(
                commentId, 
                authentication.id(),
                request,
                TargetType.COMMENT
        );
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
