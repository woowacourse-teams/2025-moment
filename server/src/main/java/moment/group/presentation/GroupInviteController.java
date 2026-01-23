package moment.group.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.group.dto.request.GroupJoinRequest;
import moment.group.dto.response.GroupJoinResponse;
import moment.group.dto.response.InviteInfoResponse;
import moment.group.service.application.GroupMemberApplicationService;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class GroupInviteController {

    private final GroupMemberApplicationService memberApplicationService;

    @PostMapping("/groups/{groupId}/invite")
    public ResponseEntity<String> createInviteLink(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        String inviteCode = memberApplicationService.createInviteLink(groupId, authentication.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(inviteCode);
    }

    @GetMapping("/invite/{code}")
    public ResponseEntity<InviteInfoResponse> getInviteInfo(@PathVariable String code) {
        InviteInfoResponse response = memberApplicationService.getInviteInfo(code);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/groups/join")
    public ResponseEntity<GroupJoinResponse> joinGroup(
            @AuthenticationPrincipal Authentication authentication,
            @Valid @RequestBody GroupJoinRequest request) {
        GroupJoinResponse response = memberApplicationService.joinGroup(authentication.id(), request);
        return ResponseEntity.ok(response);
    }
}
