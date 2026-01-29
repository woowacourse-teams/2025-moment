package moment.group.presentation;

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
import moment.like.dto.response.LikeToggleResponse;
import moment.like.service.MomentLikeService;
import moment.moment.dto.request.GroupMomentCreateRequest;
import moment.moment.dto.response.CommentableMomentResponse;
import moment.moment.dto.response.GroupMomentListResponse;
import moment.moment.dto.response.GroupMomentResponse;
import moment.moment.dto.response.MyGroupMomentListResponse;
import moment.moment.service.application.MomentApplicationService;
import moment.moment.service.facade.CommentableMomentFacadeService;
import moment.moment.service.facade.MyGroupMomentPageFacadeService;
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

@Tag(name = "Group Moment API", description = "그룹 모멘트 관련 API 명세")
@RestController
@RequestMapping("/api/v2/groups/{groupId}")
@RequiredArgsConstructor
public class GroupMomentController {

    private final MomentApplicationService momentApplicationService;
    private final MomentLikeService momentLikeService;
    private final CommentableMomentFacadeService commentableMomentFacadeService;
    private final MyGroupMomentPageFacadeService myGroupMomentPageFacadeService;

    @Operation(summary = "그룹 모멘트 작성", description = "그룹 내에 새로운 모멘트를 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "모멘트 작성 성공"),
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
    @PostMapping("/moments")
    public ResponseEntity<SuccessResponse<GroupMomentResponse>> createMoment(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @Valid @RequestBody GroupMomentCreateRequest request) {
        GroupMomentResponse response = momentApplicationService.createMomentInGroup(
            groupId, authentication.id(), request.content(), request.imageUrl(), request.imageName());
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "그룹 피드 조회", description = "그룹의 모멘트 피드를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 피드 조회 성공"),
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
    @GetMapping("/moments")
    public ResponseEntity<SuccessResponse<GroupMomentListResponse>> getGroupMoments(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @RequestParam(required = false) Long cursor) {
        GroupMomentListResponse response = momentApplicationService.getGroupMoments(groupId, authentication.id(), cursor);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "그룹 내 나의 모멘트 조회",
            description = "그룹 내에서 자신이 작성한 모멘트를 조회합니다. 댓글과 알림 정보가 포함됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "나의 모멘트 조회 성공"),
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
    @GetMapping("/my-moments")
    public ResponseEntity<SuccessResponse<MyGroupMomentListResponse>> getMyMoments(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @RequestParam(required = false) Long cursor) {
        MyGroupMomentListResponse response = myGroupMomentPageFacadeService.getMyMomentsInGroup(
                groupId, authentication.id(), cursor);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "그룹 내 읽지 않은 나의 모멘트 조회",
            description = "그룹 내에서 알림을 읽지 않은 자신의 모멘트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "읽지 않은 나의 모멘트 조회 성공"),
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
    @GetMapping("/my-moments/unread")
    public ResponseEntity<SuccessResponse<MyGroupMomentListResponse>> getUnreadMyMoments(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @RequestParam(required = false) Long cursor) {
        MyGroupMomentListResponse response = myGroupMomentPageFacadeService.getUnreadMyMomentsInGroup(
                groupId, authentication.id(), cursor);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "모멘트 삭제", description = "모멘트를 삭제합니다. 작성자만 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "모멘트 삭제 성공"),
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
    @DeleteMapping("/moments/{momentId}")
    public ResponseEntity<SuccessResponse<Void>> deleteMoment(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long momentId) {
        momentApplicationService.deleteMomentInGroup(groupId, momentId, authentication.id());
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "모멘트 좋아요 토글", description = "모멘트에 좋아요를 추가하거나 취소합니다.")
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
                    - [M-002] 존재하지 않는 모멘트입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/moments/{momentId}/like")
    public ResponseEntity<SuccessResponse<LikeToggleResponse>> toggleLike(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long momentId) {
        boolean liked = momentApplicationService.toggleMomentLike(groupId, momentId, authentication.id());
        long likeCount = momentLikeService.getCount(momentId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, LikeToggleResponse.of(liked, likeCount)));
    }

    @Operation(summary = "코멘트를 달 수 있는 그룹 모멘트 조회", description = "그룹 내에서 코멘트를 달 수 있는 모멘트를 랜덤으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "코멘트 가능 모멘트 조회 성공"),
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
    @GetMapping("/moments/commentable")
    public ResponseEntity<SuccessResponse<CommentableMomentResponse>> getCommentableMoment(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        CommentableMomentResponse response = commentableMomentFacadeService.getCommentableMomentInGroup(
                groupId, authentication.id());
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
