package moment.admin.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moment.admin.dto.response.AdminSessionDetailResponse;
import moment.admin.dto.response.AdminSessionHistoryResponse;
import moment.admin.dto.response.AdminSessionResponse;
import moment.admin.dto.response.AdminSuccessResponse;
import moment.admin.service.session.AdminSessionService;
import moment.admin.global.util.AdminSessionManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Admin Session API", description = "관리자 세션 관리 API (SUPER_ADMIN 전용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sessions")
public class AdminSessionApiController {

    private final AdminSessionService adminSessionService;
    private final AdminSessionManager adminSessionManager;

    @Operation(summary = "활성 세션 목록 조회")
    @GetMapping
    public ResponseEntity<AdminSuccessResponse<List<AdminSessionResponse>>> getActiveSessions(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String ipAddress) {

        List<AdminSessionResponse> response = adminSessionService.getFilteredActiveSessions(adminId, ipAddress);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "세션 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<AdminSuccessResponse<AdminSessionDetailResponse>> getSessionDetail(
            @PathVariable Long id) {

        AdminSessionDetailResponse response = adminSessionService.getSessionDetail(id);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "특정 세션 강제 종료")
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<AdminSuccessResponse<Void>> invalidateSession(
            @PathVariable String sessionId) {

        adminSessionManager.invalidateSessionById(sessionId);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "관리자의 모든 세션 강제 종료")
    @DeleteMapping("/admin/{adminId}")
    public ResponseEntity<AdminSuccessResponse<Void>> invalidateAllSessionsByAdmin(
            @PathVariable Long adminId) {

        adminSessionManager.invalidateAllSessionsForAdmin(adminId);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "세션 히스토리 조회")
    @GetMapping("/history")
    public ResponseEntity<AdminSuccessResponse<Page<AdminSessionHistoryResponse>>> getSessionHistory(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @PageableDefault(size = 15, sort = "loginTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AdminSessionHistoryResponse> response = adminSessionService.getSessionHistory(adminId, startDate, endDate, pageable);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }
}
