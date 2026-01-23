package moment.group.presentation;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.request.GroupUpdateRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.group.dto.response.GroupDetailResponse;
import moment.group.dto.response.MyGroupResponse;
import moment.group.service.application.GroupApplicationService;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v2/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupApplicationService groupApplicationService;

    @PostMapping
    public ResponseEntity<GroupCreateResponse> createGroup(
            @AuthenticationPrincipal Authentication authentication,
            @Valid @RequestBody GroupCreateRequest request) {
        GroupCreateResponse response = groupApplicationService.createGroup(authentication.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MyGroupResponse>> getMyGroups(
            @AuthenticationPrincipal Authentication authentication) {
        List<MyGroupResponse> response = groupApplicationService.getMyGroups(authentication.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        GroupDetailResponse response = groupApplicationService.getGroupDetail(groupId, authentication.id());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<Void> updateGroup(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @Valid @RequestBody GroupUpdateRequest request) {
        groupApplicationService.updateGroup(groupId, authentication.id(), request.name(), request.description());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        groupApplicationService.deleteGroup(groupId, authentication.id());
        return ResponseEntity.noContent().build();
    }
}
