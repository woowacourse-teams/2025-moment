package moment.group.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.like.dto.response.LikeToggleResponse;
import moment.like.service.MomentLikeService;
import moment.moment.dto.request.GroupMomentCreateRequest;
import moment.moment.dto.response.GroupFeedResponse;
import moment.moment.dto.response.GroupMomentResponse;
import moment.moment.service.application.MomentApplicationService;
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

@RestController
@RequestMapping("/api/v2/groups/{groupId}")
@RequiredArgsConstructor
public class GroupMomentController {

    private final MomentApplicationService momentApplicationService;
    private final MomentLikeService momentLikeService;

    @PostMapping("/moments")
    public ResponseEntity<GroupMomentResponse> createMoment(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @Valid @RequestBody GroupMomentCreateRequest request) {
        GroupMomentResponse response = momentApplicationService.createMomentInGroup(
            groupId, authentication.id(), request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/moments")
    public ResponseEntity<GroupFeedResponse> getGroupFeed(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @RequestParam(required = false) Long cursor) {
        GroupFeedResponse response = momentApplicationService.getGroupFeed(groupId, authentication.id(), cursor);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-moments")
    public ResponseEntity<GroupFeedResponse> getMyMoments(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @RequestParam(required = false) Long cursor) {
        GroupFeedResponse response = momentApplicationService.getMyMomentsInGroup(groupId, authentication.id(), cursor);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/moments/{momentId}")
    public ResponseEntity<Void> deleteMoment(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long momentId) {
        momentApplicationService.deleteMomentInGroup(groupId, momentId, authentication.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/moments/{momentId}/like")
    public ResponseEntity<LikeToggleResponse> toggleLike(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long momentId) {
        boolean liked = momentApplicationService.toggleMomentLike(groupId, momentId, authentication.id());
        long likeCount = momentLikeService.getCount(momentId);
        return ResponseEntity.ok(LikeToggleResponse.of(liked, likeCount));
    }
}
