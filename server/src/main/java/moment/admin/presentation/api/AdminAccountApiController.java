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
import moment.admin.dto.request.AdminCreateRequest;
import moment.admin.dto.response.AdminAccountListResponse;
import moment.admin.dto.response.AdminAccountResponse;
import moment.admin.dto.response.AdminErrorResponse;
import moment.admin.dto.response.AdminSuccessResponse;
import moment.admin.global.util.AdminSessionManager;
import moment.admin.service.admin.AdminService;
import moment.admin.service.application.AdminManagementApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Account API", description = "관리자 계정 관리 API (SUPER_ADMIN 전용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/accounts")
public class AdminAccountApiController {

    private final AdminService adminService;
    private final AdminManagementApplicationService adminManagementApplicationService;
    private final AdminSessionManager sessionManager;

    @Operation(summary = "관리자 목록 조회", description = "전체 관리자 계정 목록을 페이지네이션으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관리자 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "[A-008] 세션을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<AdminSuccessResponse<Page<AdminAccountListResponse>>> getAccounts(
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Admin> admins = adminService.getAllAdmins(pageable);
        Page<AdminAccountListResponse> response = admins.map(AdminAccountListResponse::from);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "관리자 생성", description = "새로운 관리자 계정을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "관리자 생성 성공"),
            @ApiResponse(responseCode = "400", description = "[A-005] 유효하지 않은 입력 정보입니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "[A-008] 세션을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "[A-004] 이미 등록된 관리자 이메일입니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AdminSuccessResponse<AdminAccountResponse>> createAccount(
            @Valid @RequestBody AdminCreateRequest request) {

        Admin admin = adminService.createAdmin(request.email(), request.name(), request.password());
        AdminAccountResponse response = AdminAccountResponse.from(admin);

        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "관리자 차단", description = "관리자 계정을 차단하고 해당 관리자의 모든 세션을 종료합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관리자 차단 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [A-006] 자기 자신을 차단할 수 없습니다.
                    - [A-007] 마지막 SUPER_ADMIN은 차단할 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-002] 존재하지 않는 관리자입니다.
                    - [A-008] 세션을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @PostMapping("/{id}/block")
    public ResponseEntity<AdminSuccessResponse<Void>> blockAccount(
            @PathVariable Long id,
            HttpSession session) {

        Long currentAdminId = sessionManager.getId(session);
        adminManagementApplicationService.blockAdminAndInvalidateSessions(currentAdminId, id);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "관리자 차단 해제", description = "차단된 관리자 계정의 차단을 해제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관리자 차단 해제 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-002] 존재하지 않는 관리자입니다.
                    - [A-008] 세션을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @PostMapping("/{id}/unblock")
    public ResponseEntity<AdminSuccessResponse<Void>> unblockAccount(
            @PathVariable Long id) {

        adminService.unblockAdmin(id);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }
}
