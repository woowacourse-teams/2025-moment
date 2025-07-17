package moment.comment.presentation;

import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.comment.application.CommentService;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.global.dto.response.SuccessResponse;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<SuccessResponse<CommentCreateResponse>> createComment(
            @RequestBody CommentCreateRequest request, @AuthenticationPrincipal Authentication authentication) {
        Long userId = authentication.id();
        CommentCreateResponse response = commentService.addComment(request, userId);
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
