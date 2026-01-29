package moment.group.presentation;

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
import moment.comment.dto.request.GroupCommentCreateRequest;
import moment.comment.dto.response.GroupCommentResponse;
import moment.comment.dto.response.MyGroupCommentListResponse;
import moment.comment.service.application.CommentApplicationService;
import moment.comment.service.facade.MyGroupCommentPageFacadeService;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.like.dto.response.LikeToggleResponse;
import moment.like.service.CommentLikeService;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Group Comment API", description = "그룹 코멘트 관련 API 명세")
@RestController
@RequestMapping("/api/v2/groups/{groupId}")
@RequiredArgsConstructor
public class GroupCommentController {

    private final CommentApplicationService commentApplicationService;
    private final CommentLikeService commentLikeService;
    private final MyGroupCommentPageFacadeService myGroupCommentPageFacadeService;

    @Operation(summary = "코멘트 작성", description = "모멘트에 코멘트를 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "코멘트 작성 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GM-002] 그룹 멤버가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    - [M-002] 존재하지 않는 모멘트입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/moments/{momentId}/comments")
    public ResponseEntity<SuccessResponse<GroupCommentResponse>> createComment(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long momentId,
            @Valid @RequestBody GroupCommentCreateRequest request) {
        GroupCommentResponse response = commentApplicationService.createCommentInGroup(
            groupId, momentId, authentication.id(), request.content(), request.imageUrl(), request.imageName());
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "코멘트 목록 조회", description = "모멘트의 코멘트 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "코멘트 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GM-002] 그룹 멤버가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    - [M-002] 존재하지 않는 모멘트입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/moments/{momentId}/comments")
    public ResponseEntity<SuccessResponse<List<GroupCommentResponse>>> getComments(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long momentId) {
        List<GroupCommentResponse> response = commentApplicationService.getCommentsInGroup(
            groupId, momentId, authentication.id());
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "코멘트 삭제", description = "코멘트를 삭제합니다. 작성자만 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "코멘트 삭제 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GM-002] 그룹 멤버가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    - [C-002] 존재하지 않는 코멘트입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<SuccessResponse<Void>> deleteComment(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long commentId) {
        commentApplicationService.deleteCommentInGroup(groupId, commentId, authentication.id());
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "코멘트 좋아요 토글", description = "코멘트에 좋아요를 추가하거나 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 토글 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GM-002] 그룹 멤버가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    - [C-002] 존재하지 않는 코멘트입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<SuccessResponse<LikeToggleResponse>> toggleLike(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long commentId) {
        boolean liked = commentApplicationService.toggleCommentLike(groupId, commentId, authentication.id());
        long likeCount = commentLikeService.getCount(commentId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, LikeToggleResponse.of(liked, likeCount)));
    }

    @Operation(summary = "그룹 내 나의 코멘트 조회",
            description = "그룹 내에서 자신이 작성한 코멘트를 조회합니다. 알림 정보가 포함됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "나의 코멘트 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GM-002] 그룹 멤버가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/my-comments")
    public ResponseEntity<SuccessResponse<MyGroupCommentListResponse>> getMyComments(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @RequestParam(required = false) Long cursor) {
        MyGroupCommentListResponse response = myGroupCommentPageFacadeService.getMyCommentsInGroup(
                groupId, authentication.id(), cursor);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "그룹 내 읽지 않은 나의 코멘트 조회",
            description = "그룹 내에서 알림을 읽지 않은 자신의 코멘트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "읽지 않은 나의 코멘트 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GM-002] 그룹 멤버가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/my-comments/unread")
    public ResponseEntity<SuccessResponse<MyGroupCommentListResponse>> getUnreadMyComments(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @RequestParam(required = false) Long cursor) {
        MyGroupCommentListResponse response = myGroupCommentPageFacadeService.getUnreadMyCommentsInGroup(
                groupId, authentication.id(), cursor);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
