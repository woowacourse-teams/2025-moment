package moment.question.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.question.domain.QuestionCycle;
import moment.question.dto.response.QuestionResponse;
import moment.question.service.facade.QuestionFacadeService;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionFacadeService questionFacadeService;

    @Operation(summary = "현재 질문 조회", description = "그룹의 현재 질문을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "현재 질문 조회 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [Q-002] 지원하지 않는 질문 사이클입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    - [Q-001] 존재하지 않는 질문입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

    })
    @GetMapping("/current")
    public ResponseEntity<SuccessResponse<QuestionResponse>> getCurrentQuestion(
            @RequestParam(value = "groupId", required = false) Long groupId,
            @RequestParam(value = "cycle", defaultValue = "WEEKLY") QuestionCycle cycle,
            @AuthenticationPrincipal Authentication authentication
    ) {
        Long id = authentication.id();
        QuestionResponse response = questionFacadeService.findCurrentQuestion(groupId, id, cycle);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
