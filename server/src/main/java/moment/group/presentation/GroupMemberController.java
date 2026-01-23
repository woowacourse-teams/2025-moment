package moment.group.presentation;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.group.dto.request.ProfileUpdateRequest;
import moment.group.dto.response.MemberResponse;
import moment.group.service.application.GroupMemberApplicationService;
import moment.user.dto.request.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/groups/{groupId}")
@RequiredArgsConstructor
public class GroupMemberController {

    private final GroupMemberApplicationService memberApplicationService;

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getMembers(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        List<MemberResponse> response = memberApplicationService.getMembers(groupId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<MemberResponse>> getPendingMembers(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        List<MemberResponse> response = memberApplicationService.getPendingMembers(groupId, authentication.id());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        memberApplicationService.updateProfile(groupId, authentication.id(), request.nickname());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/leave")
    public ResponseEntity<Void> leaveGroup(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        memberApplicationService.leaveGroup(groupId, authentication.id());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<Void> kickMember(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        memberApplicationService.kickMember(groupId, memberId, authentication.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/members/{memberId}/approve")
    public ResponseEntity<Void> approveMember(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        memberApplicationService.approveMember(groupId, memberId, authentication.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/members/{memberId}/reject")
    public ResponseEntity<Void> rejectMember(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        memberApplicationService.rejectMember(groupId, memberId, authentication.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer/{memberId}")
    public ResponseEntity<Void> transferOwnership(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        memberApplicationService.transferOwnership(groupId, authentication.id(), memberId);
        return ResponseEntity.noContent().build();
    }
}
