package moment.comment.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.reply.application.EchoService;
import moment.reply.dto.request.EchoCreateRequest;
import moment.comment.dto.tobe.EchoDetail;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Echo API", description = "에코 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/echos")
public class EchoController {

    private final EchoService echoService;

    @Operation(summary = "에코 등록", description = "새로운 에코를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "에코 등록 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [E-001] "존재하지 않는 에코입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = """
                    - [E-002] 해당 에코가 이미 존재합니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping()
    public ResponseEntity<SuccessResponse<Void>> createEchos(
            @Valid @RequestBody EchoCreateRequest request,
            @AuthenticationPrincipal Authentication authentication
    ) {
        echoService.addEchos(request, authentication);
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "에코 조회", description = "코멘트에 달린 모든 에코를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "에코 조회 성공")
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<SuccessResponse<List<EchoDetail>>> readEchosByComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal Authentication authentication
    ) {
        List<EchoDetail> response = echoService.getEchosByCommentId(commentId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
