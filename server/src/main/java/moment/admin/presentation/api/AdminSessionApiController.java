package moment.admin.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moment.admin.dto.response.AdminErrorResponse;
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

    @Operation(summary = "활성 세션 목록 조회", description = "현재 활성화된 관리자 세션 목록을 필터링하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "활성 세션 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "[A-008] 세션을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<AdminSuccessResponse<List<AdminSessionResponse>>> getActiveSessions(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String ipAddress) {

        List<AdminSessionResponse> response = adminSessionService.getFilteredActiveSessions(adminId, ipAddress);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "세션 상세 조회", description = "특정 세션의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "세션 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "[A-008] 세션을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminSuccessResponse<AdminSessionDetailResponse>> getSessionDetail(
            @PathVariable Long id) {

        AdminSessionDetailResponse response = adminSessionService.getSessionDetail(id);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "특정 세션 강제 종료", description = "세션 ID로 특정 세션을 강제 종료합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "세션 강제 종료 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "[A-008] 세션을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<AdminSuccessResponse<Void>> invalidateSession(
            @PathVariable String sessionId) {

        adminSessionManager.invalidateSessionById(sessionId);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "관리자의 모든 세션 강제 종료", description = "특정 관리자의 모든 활성 세션을 강제 종료합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관리자의 모든 세션 강제 종료 성공"),
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
    @DeleteMapping("/admin/{adminId}")
    public ResponseEntity<AdminSuccessResponse<Void>> invalidateAllSessionsByAdmin(
            @PathVariable Long adminId) {

        adminSessionManager.invalidateAllSessionsForAdmin(adminId);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "세션 히스토리 조회", description = "관리자 세션 히스토리를 필터링하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "세션 히스토리 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "[A-003] 관리자 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "[A-008] 세션을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
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
