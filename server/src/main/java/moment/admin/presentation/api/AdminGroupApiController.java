package moment.admin.presentation.api;

import lombok.RequiredArgsConstructor;
import moment.admin.domain.GroupStatusFilter;
import moment.admin.dto.response.AdminGroupDetailResponse;
import moment.admin.dto.response.AdminGroupListResponse;
import moment.admin.dto.response.AdminGroupMemberListResponse;
import moment.admin.dto.response.AdminGroupStatsResponse;
import moment.admin.service.group.AdminGroupQueryService;
import moment.global.dto.response.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/groups")
@RequiredArgsConstructor
public class AdminGroupApiController {

    private final AdminGroupQueryService adminGroupQueryService;

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
}
