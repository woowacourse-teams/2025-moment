package moment.admin.presentation.api;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.domain.Admin;
import moment.admin.domain.GroupStatusFilter;
import moment.admin.dto.request.AdminGroupUpdateRequest;
import moment.admin.domain.AdminGroupLogType;
import moment.admin.dto.response.AdminCommentListResponse;
import moment.admin.dto.response.AdminGroupDetailResponse;
import moment.admin.dto.response.AdminGroupInviteLinkResponse;
import moment.admin.dto.response.AdminGroupListResponse;
import moment.admin.dto.response.AdminGroupLogListResponse;
import moment.admin.dto.response.AdminGroupMemberListResponse;
import moment.admin.dto.response.AdminGroupStatsResponse;
import moment.admin.dto.response.AdminMomentListResponse;
import moment.admin.global.util.AdminSessionManager;
import moment.admin.service.admin.AdminService;
import moment.admin.service.content.AdminContentService;
import moment.admin.service.group.AdminGroupLogService;
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
    private final AdminGroupLogService adminGroupLogService;
    private final AdminContentService adminContentService;
    private final AdminSessionManager adminSessionManager;
    private final AdminService adminService;

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

    // ===== 초대 링크 API =====

    @GetMapping("/{groupId}/invite-link")
    public ResponseEntity<SuccessResponse<AdminGroupInviteLinkResponse>> getInviteLink(
        @PathVariable Long groupId
    ) {
        AdminGroupInviteLinkResponse response = adminGroupService.getInviteLink(groupId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    // ===== Admin 로그 조회 API =====

    @GetMapping("/logs")
    public ResponseEntity<SuccessResponse<AdminGroupLogListResponse>> getGroupLogs(
        @RequestParam(required = false) Long groupId,
        @RequestParam(required = false) AdminGroupLogType type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        AdminGroupLogListResponse response = adminGroupLogService.getGroupLogs(groupId, type, page, size);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    // ===== 콘텐츠 관리 API =====

    @GetMapping("/{groupId}/moments")
    public ResponseEntity<SuccessResponse<AdminMomentListResponse>> getMoments(
        @PathVariable Long groupId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        AdminMomentListResponse response = adminContentService.getMoments(groupId, page, size);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @DeleteMapping("/{groupId}/moments/{momentId}")
    public ResponseEntity<SuccessResponse<Void>> deleteMoment(
        @PathVariable Long groupId,
        @PathVariable Long momentId,
        HttpSession session
    ) {
        Long adminId = adminSessionManager.getId(session);
        Admin admin = adminService.getAdminById(adminId);
        adminContentService.deleteMoment(groupId, momentId, adminId, admin.getEmail());
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @GetMapping("/{groupId}/moments/{momentId}/comments")
    public ResponseEntity<SuccessResponse<AdminCommentListResponse>> getComments(
        @PathVariable Long groupId,
        @PathVariable Long momentId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        AdminCommentListResponse response = adminContentService.getComments(groupId, momentId, page, size);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @DeleteMapping("/{groupId}/comments/{commentId}")
    public ResponseEntity<SuccessResponse<Void>> deleteComment(
        @PathVariable Long groupId,
        @PathVariable Long commentId,
        HttpSession session
    ) {
        Long adminId = adminSessionManager.getId(session);
        Admin admin = adminService.getAdminById(adminId);
        adminContentService.deleteComment(groupId, commentId, adminId, admin.getEmail());
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
