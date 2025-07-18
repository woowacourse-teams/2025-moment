package moment.reply.presentation;

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
import moment.reply.application.EmojiService;
import moment.reply.dto.request.EmojiCreateRequest;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Emoji API", description = "이모지 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/emoji")
public class EmojiController {

    private final EmojiService emojiService;

    @Operation(summary = "이모지 등록", description = "새로운 이모지를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "이모지 등록 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [E-001] "존재하지 않는 이모지입니다.
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
    })
    @PostMapping()
    public ResponseEntity<SuccessResponse<Void>> createEmoji(
            @RequestBody EmojiCreateRequest request,
            @AuthenticationPrincipal Authentication authentication
    ) {
        emojiService.addEmoji(request, authentication);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
