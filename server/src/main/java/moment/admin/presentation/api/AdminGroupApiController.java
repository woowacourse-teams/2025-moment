package moment.admin.presentation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.domain.GroupStatusFilter;
import moment.admin.dto.request.AdminGroupUpdateRequest;
import moment.admin.dto.response.AdminGroupDetailResponse;
import moment.admin.dto.response.AdminGroupListResponse;
import moment.admin.dto.response.AdminGroupMemberListResponse;
import moment.admin.dto.response.AdminGroupStatsResponse;
import moment.admin.service.group.AdminGroupMemberService;
import moment.admin.service.group.AdminGroupQueryService;
import moment.admin.service.group.AdminGroupService;
import moment.global.dto.response.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/groups")
@RequiredArgsConstructor
public class AdminGroupApiController {

    private final AdminGroupQueryService adminGroupQueryService;
    private final AdminGroupService adminGroupService;
    private final AdminGroupMemberService adminGroupMemberService;

    @GetMapping("/stats")
    public ResponseEntity<SuccessResponse<AdminGroupStatsResponse>> getGroupStats() {
        AdminGroupStatsResponse response = adminGroupQueryService.getGroupStats();
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<AdminGroupListResponse>> getGroupList(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "ALL") GroupStatusFilter status
    ) {
        AdminGroupListResponse response = adminGroupQueryService.getGroupList(page, size, keyword, status);
        HttpStatus httpStatus = HttpStatus.OK;
        return ResponseEntity.status(httpStatus).body(SuccessResponse.of(httpStatus, response));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<SuccessResponse<AdminGroupDetailResponse>> getGroupDetail(
        @PathVariable Long groupId
    ) {
        AdminGroupDetailResponse response = adminGroupQueryService.getGroupDetail(groupId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<SuccessResponse<AdminGroupMemberListResponse>> getApprovedMembers(
        @PathVariable Long groupId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        AdminGroupMemberListResponse response = adminGroupQueryService.getApprovedMembers(groupId, page, size);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @GetMapping("/{groupId}/pending-members")
    public ResponseEntity<SuccessResponse<AdminGroupMemberListResponse>> getPendingMembers(
        @PathVariable Long groupId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        AdminGroupMemberListResponse response = adminGroupQueryService.getPendingMembers(groupId, page, size);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<SuccessResponse<Void>> updateGroup(
        @PathVariable Long groupId,
        @Valid @RequestBody AdminGroupUpdateRequest request
    ) {
        adminGroupService.updateGroup(groupId, request);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<SuccessResponse<Void>> deleteGroup(
        @PathVariable Long groupId
    ) {
        adminGroupService.deleteGroup(groupId);
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @PostMapping("/{groupId}/restore")
    public ResponseEntity<SuccessResponse<Void>> restoreGroup(
        @PathVariable Long groupId
    ) {
        adminGroupService.restoreGroup(groupId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    // ===== 멤버 관리 API =====

    @PostMapping("/{groupId}/members/{memberId}/approve")
    public ResponseEntity<SuccessResponse<Void>> approveMember(
        @PathVariable Long groupId,
        @PathVariable Long memberId
    ) {
        adminGroupMemberService.approveMember(groupId, memberId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @PostMapping("/{groupId}/members/{memberId}/reject")
    public ResponseEntity<SuccessResponse<Void>> rejectMember(
        @PathVariable Long groupId,
        @PathVariable Long memberId
    ) {
        adminGroupMemberService.rejectMember(groupId, memberId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<SuccessResponse<Void>> kickMember(
        @PathVariable Long groupId,
        @PathVariable Long memberId
    ) {
        adminGroupMemberService.kickMember(groupId, memberId);
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @PostMapping("/{groupId}/transfer-ownership/{newOwnerMemberId}")
    public ResponseEntity<SuccessResponse<Void>> transferOwnership(
        @PathVariable Long groupId,
        @PathVariable Long newOwnerMemberId
    ) {
        adminGroupMemberService.transferOwnership(groupId, newOwnerMemberId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
