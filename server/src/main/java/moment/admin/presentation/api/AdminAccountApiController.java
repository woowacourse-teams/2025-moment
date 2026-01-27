package moment.admin.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminCreateRequest;
import moment.admin.dto.response.AdminAccountListResponse;
import moment.admin.dto.response.AdminAccountResponse;
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

    @Operation(summary = "관리자 목록 조회")
    @GetMapping
    public ResponseEntity<AdminSuccessResponse<Page<AdminAccountListResponse>>> getAccounts(
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Admin> admins = adminService.getAllAdmins(pageable);
        Page<AdminAccountListResponse> response = admins.map(AdminAccountListResponse::from);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "관리자 생성")
    @PostMapping
    public ResponseEntity<AdminSuccessResponse<AdminAccountResponse>> createAccount(
            @Valid @RequestBody AdminCreateRequest request) {

        Admin admin = adminService.createAdmin(request.email(), request.name(), request.password());
        AdminAccountResponse response = AdminAccountResponse.from(admin);

        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "관리자 차단")
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

    @Operation(summary = "관리자 차단 해제")
    @PostMapping("/{id}/unblock")
    public ResponseEntity<AdminSuccessResponse<Void>> unblockAccount(
            @PathVariable Long id) {

        adminService.unblockAdmin(id);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }
}
