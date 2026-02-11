package moment.admin.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.domain.Admin;
import moment.admin.domain.GroupStatusFilter;
import moment.admin.dto.request.AdminGroupUpdateRequest;
import moment.admin.domain.AdminGroupLogType;
import moment.admin.dto.response.AdminCommentListResponse;
import moment.admin.dto.response.AdminErrorResponse;
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
import moment.admin.dto.response.AdminSuccessResponse;
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

@Tag(name = "Admin Group API", description = "관리자용 그룹 관리 API")
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

    @Operation(summary = "그룹 통계 조회", description = "전체 그룹 통계 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 통계 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "[A-008] 세션을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping("/stats")
    public ResponseEntity<AdminSuccessResponse<AdminGroupStatsResponse>> getGroupStats() {
        AdminGroupStatsResponse response = adminGroupQueryService.getGroupStats();
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "그룹 목록 조회", description = "그룹 목록을 페이지네이션과 필터링으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "[A-008] 세션을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<AdminSuccessResponse<AdminGroupListResponse>> getGroupList(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "ALL") GroupStatusFilter status
    ) {
        AdminGroupListResponse response = adminGroupQueryService.getGroupList(page, size, keyword, status);
        HttpStatus httpStatus = HttpStatus.OK;
        return ResponseEntity.status(httpStatus).body(AdminSuccessResponse.of(httpStatus, response));
    }

    @Operation(summary = "그룹 상세 조회", description = "특정 그룹의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping("/{groupId}")
    public ResponseEntity<AdminSuccessResponse<AdminGroupDetailResponse>> getGroupDetail(
        @PathVariable Long groupId
    ) {
        AdminGroupDetailResponse response = adminGroupQueryService.getGroupDetail(groupId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "승인된 멤버 목록 조회", description = "그룹의 승인된 멤버 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인된 멤버 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping("/{groupId}/members")
    public ResponseEntity<AdminSuccessResponse<AdminGroupMemberListResponse>> getApprovedMembers(
        @PathVariable Long groupId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        AdminGroupMemberListResponse response = adminGroupQueryService.getApprovedMembers(groupId, page, size);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "대기 중인 멤버 목록 조회", description = "그룹의 승인 대기 중인 멤버 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "대기 중인 멤버 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping("/{groupId}/pending-members")
    public ResponseEntity<AdminSuccessResponse<AdminGroupMemberListResponse>> getPendingMembers(
        @PathVariable Long groupId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        AdminGroupMemberListResponse response = adminGroupQueryService.getPendingMembers(groupId, page, size);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "그룹 정보 수정", description = "그룹의 이름과 설명을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "[AG-003] 이미 삭제된 그룹입니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @PutMapping("/{groupId}")
    public ResponseEntity<AdminSuccessResponse<Void>> updateGroup(
        @PathVariable Long groupId,
        @Valid @RequestBody AdminGroupUpdateRequest request
    ) {
        adminGroupService.updateGroup(groupId, request);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "그룹 삭제", description = "그룹을 삭제합니다 (Soft Delete).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "그룹 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "[AG-003] 이미 삭제된 그룹입니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @DeleteMapping("/{groupId}")
    public ResponseEntity<AdminSuccessResponse<Void>> deleteGroup(
        @PathVariable Long groupId
    ) {
        adminGroupService.deleteGroup(groupId);
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "삭제된 그룹 복구", description = "삭제된 그룹을 복구합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 복구 성공"),
            @ApiResponse(responseCode = "400", description = "[AG-002] 삭제되지 않은 그룹은 복원할 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @PostMapping("/{groupId}/restore")
    public ResponseEntity<AdminSuccessResponse<Void>> restoreGroup(
        @PathVariable Long groupId
    ) {
        adminGroupService.restoreGroup(groupId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, null));
    }

    // ===== 멤버 관리 API =====

    @Operation(summary = "멤버 승인", description = "대기 중인 멤버를 승인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "멤버 승인 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [AM-003] 승인 대기 중인 멤버가 아닙니다.
                    - [AM-006] 이미 승인된 멤버입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    - [AM-001] 멤버를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @PostMapping("/{groupId}/members/{memberId}/approve")
    public ResponseEntity<AdminSuccessResponse<Void>> approveMember(
        @PathVariable Long groupId,
        @PathVariable Long memberId
    ) {
        adminGroupMemberService.approveMember(groupId, memberId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "멤버 거절", description = "대기 중인 멤버의 가입을 거절합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "멤버 거절 성공"),
            @ApiResponse(responseCode = "400", description = "[AM-003] 승인 대기 중인 멤버가 아닙니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    - [AM-001] 멤버를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @PostMapping("/{groupId}/members/{memberId}/reject")
    public ResponseEntity<AdminSuccessResponse<Void>> rejectMember(
        @PathVariable Long groupId,
        @PathVariable Long memberId
    ) {
        adminGroupMemberService.rejectMember(groupId, memberId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "멤버 강제 퇴장", description = "승인된 멤버를 그룹에서 강제 퇴장시킵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "멤버 강제 퇴장 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [AM-002] 그룹장은 추방할 수 없습니다.
                    - [AM-004] 승인된 멤버만 그룹장이 될 수 있습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    - [AM-001] 멤버를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<AdminSuccessResponse<Void>> kickMember(
        @PathVariable Long groupId,
        @PathVariable Long memberId
    ) {
        adminGroupMemberService.kickMember(groupId, memberId);
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "방장 권한 이전", description = "그룹의 방장 권한을 다른 멤버에게 이전합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "방장 권한 이전 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [AM-004] 승인된 멤버만 그룹장이 될 수 있습니다.
                    - [AM-005] 이미 그룹장인 멤버입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    - [AM-001] 멤버를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @PostMapping("/{groupId}/transfer-ownership/{newOwnerMemberId}")
    public ResponseEntity<AdminSuccessResponse<Void>> transferOwnership(
        @PathVariable Long groupId,
        @PathVariable Long newOwnerMemberId
    ) {
        adminGroupMemberService.transferOwnership(groupId, newOwnerMemberId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, null));
    }

    // ===== 초대 링크 API =====

    @Operation(summary = "초대 링크 조회", description = "그룹의 초대 링크 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초대 링크 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping("/{groupId}/invite-link")
    public ResponseEntity<AdminSuccessResponse<AdminGroupInviteLinkResponse>> getInviteLink(
        @PathVariable Long groupId
    ) {
        AdminGroupInviteLinkResponse response = adminGroupService.getInviteLink(groupId);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, response));
    }

    // ===== Admin 로그 조회 API =====

    @Operation(summary = "Admin 로그 조회", description = "관리자의 그룹 관련 활동 로그를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin 로그 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "[A-008] 세션을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping("/logs")
    public ResponseEntity<AdminSuccessResponse<AdminGroupLogListResponse>> getGroupLogs(
        @RequestParam(required = false) Long groupId,
        @RequestParam(required = false) AdminGroupLogType type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        AdminGroupLogListResponse response = adminGroupLogService.getGroupLogs(groupId, type, page, size);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, response));
    }

    // ===== 콘텐츠 관리 API =====

    @Operation(summary = "그룹 내 모멘트 목록 조회", description = "그룹에 속한 모멘트 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "모멘트 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping("/{groupId}/moments")
    public ResponseEntity<AdminSuccessResponse<AdminMomentListResponse>> getMoments(
        @PathVariable Long groupId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        AdminMomentListResponse response = adminContentService.getMoments(groupId, page, size);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "모멘트 삭제", description = "특정 모멘트를 삭제합니다 (Soft Delete).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "모멘트 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "[AC-003] 이미 삭제된 모멘트입니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    - [AC-001] 모멘트를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @DeleteMapping("/{groupId}/moments/{momentId}")
    public ResponseEntity<AdminSuccessResponse<Void>> deleteMoment(
        @PathVariable Long groupId,
        @PathVariable Long momentId,
        HttpSession session
    ) {
        Long adminId = adminSessionManager.getId(session);
        Admin admin = adminService.getAdminById(adminId);
        adminContentService.deleteMoment(groupId, momentId, adminId, admin.getEmail());
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "모멘트 내 댓글 목록 조회", description = "특정 모멘트에 달린 댓글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    - [AC-001] 모멘트를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping("/{groupId}/moments/{momentId}/comments")
    public ResponseEntity<AdminSuccessResponse<AdminCommentListResponse>> getComments(
        @PathVariable Long groupId,
        @PathVariable Long momentId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        AdminCommentListResponse response = adminContentService.getComments(groupId, momentId, page, size);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다 (Soft Delete).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "[AC-004] 이미 삭제된 코멘트입니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-008] 세션을 찾을 수 없습니다.
                    - [AG-001] 그룹을 찾을 수 없습니다.
                    - [AC-002] 코멘트를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @DeleteMapping("/{groupId}/comments/{commentId}")
    public ResponseEntity<AdminSuccessResponse<Void>> deleteComment(
        @PathVariable Long groupId,
        @PathVariable Long commentId,
        HttpSession session
    ) {
        Long adminId = adminSessionManager.getId(session);
        Admin admin = adminService.getAdminById(adminId);
        adminContentService.deleteComment(groupId, commentId, adminId, admin.getEmail());
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, null));
    }
}
