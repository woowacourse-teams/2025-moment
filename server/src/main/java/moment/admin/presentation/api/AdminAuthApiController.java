package moment.admin.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.dto.response.AdminErrorResponse;
import moment.admin.dto.response.AdminLoginResponse;
import moment.admin.dto.response.AdminMeResponse;
import moment.admin.dto.response.AdminSuccessResponse;
import moment.admin.global.util.AdminSessionManager;
import moment.admin.global.util.ClientIpExtractor;
import moment.admin.service.admin.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Auth API", description = "관리자 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class AdminAuthApiController {

    private final AdminService adminService;
    private final AdminSessionManager sessionManager;

    @Operation(summary = "관리자 로그인", description = "관리자 이메일과 비밀번호로 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "[A-005] 유효하지 않은 입력 정보입니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "[A-001] 관리자 로그인에 실패했습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AdminSuccessResponse<AdminLoginResponse>> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpSession session,
            HttpServletRequest httpRequest) {

        Admin admin = adminService.authenticateAdmin(request.email(), request.password());

        // 세션 고정 공격 방지
        httpRequest.changeSessionId();

        String ipAddress = ClientIpExtractor.extract(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        sessionManager.registerSession(session, admin.getId(), admin.getRole(), ipAddress, userAgent);

        AdminLoginResponse response = AdminLoginResponse.from(admin);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "관리자 로그아웃", description = "현재 세션을 종료하고 로그아웃합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "[A-008] 세션을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<AdminSuccessResponse<Void>> logout(HttpSession session) {
        sessionManager.invalidate(session);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "현재 관리자 정보 조회", description = "현재 로그인한 관리자의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관리자 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A-009] 세션이 만료되었습니다.",
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [A-002] 존재하지 않는 관리자입니다.
                    - [A-008] 세션을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = AdminErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<AdminSuccessResponse<AdminMeResponse>> getCurrentAdmin(HttpSession session) {
        Long adminId = sessionManager.getId(session);
        Admin admin = adminService.getAdminById(adminId);

        AdminMeResponse response = AdminMeResponse.from(admin);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }
}
