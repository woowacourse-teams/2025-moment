package moment.group.presentation;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.comment.dto.request.GroupCommentCreateRequest;
import moment.comment.dto.response.GroupCommentResponse;
import moment.comment.service.application.CommentApplicationService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/groups/{groupId}")
@RequiredArgsConstructor
public class GroupCommentController {

    private final CommentApplicationService commentApplicationService;
    private final CommentLikeService commentLikeService;

    @PostMapping("/moments/{momentId}/comments")
    public ResponseEntity<GroupCommentResponse> createComment(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long momentId,
            @Valid @RequestBody GroupCommentCreateRequest request) {
        GroupCommentResponse response = commentApplicationService.createCommentInGroup(
            groupId, momentId, authentication.id(), request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/moments/{momentId}/comments")
    public ResponseEntity<List<GroupCommentResponse>> getComments(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long momentId) {
        List<GroupCommentResponse> response = commentApplicationService.getCommentsInGroup(
            groupId, momentId, authentication.id());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long commentId) {
        commentApplicationService.deleteCommentInGroup(groupId, commentId, authentication.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<LikeToggleResponse> toggleLike(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long commentId) {
        boolean liked = commentApplicationService.toggleCommentLike(groupId, commentId, authentication.id());
        long likeCount = commentLikeService.getCount(commentId);
        return ResponseEntity.ok(LikeToggleResponse.of(liked, likeCount));
    }
}
