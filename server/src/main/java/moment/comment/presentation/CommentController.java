package moment.comment.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.dto.response.MyCommentPageResponse;
import moment.comment.service.facade.CommentCreateFacadeService;
import moment.comment.service.facade.MyCommentPageFacadeService;
import moment.comment.service.tobe.application.CommentApplicationService;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Comment API", description = "Comment 관련 API 명세")
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentCreateFacadeService commentCreateFacadeService;
    private final MyCommentPageFacadeService myCommentPageFacadeService;
    private final CommentApplicationService commentApplicationService;

    @Operation(summary = "Comment 등록", description = "새로운 Comment를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment 등록 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [C-002] 유효하지 않은 코멘트입니다.
                    - [C-004] 유효하지 않은 코멘트 형식입니다.
                    - [C-005] 유효하지 않은 코멘트 ID입니다.
                    - [G-002] 유효하지 않은 요청 값입니다.
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
                    - [M-001] 존재하지 않는 모멘트입니다.
                    - [C-002] 존재하지 않는 코멘트입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<SuccessResponse<CommentCreateResponse>> createComment(
            @Valid @RequestBody CommentCreateRequest request, @AuthenticationPrincipal Authentication authentication) {
        Long userId = authentication.id();
        CommentCreateResponse response = commentCreateFacadeService.createComment(request, userId);
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "나의 Comment 목록 조회", description = "내가 등록한 Comment 목록을 생성 시간 내림 차순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "나의 Comment 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    - [C-006] 유효하지 않은 페이지 사이즈입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<MyCommentPageResponse>> readMyComments(
            @RequestParam(required = false) String nextCursor,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal Authentication authentication) {
        Long userId = authentication.id();
        MyCommentPageResponse response = myCommentPageFacadeService.getMyCommentsPage(nextCursor, limit, userId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "알림을 확인하지 않은 나의 Comment 목록 조회", description = "내가 등록한 Comment 목록 중 알림을 확인하지 않은 것을 생성 시간 내림차순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "나의 Comment 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    - [C-006] 유효하지 않은 페이지 사이즈입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me/unread")
    public ResponseEntity<SuccessResponse<MyCommentPageResponse>> readMyUnreadComments(
            @RequestParam(required = false) String nextCursor,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal Authentication authentication) {

        Long userId = authentication.id();
        MyCommentPageResponse response = commentService.getMyUnreadComments(nextCursor, limit, userId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
