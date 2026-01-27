package moment.admin.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.dto.request.AdminUserUpdateRequest;
import moment.admin.dto.response.AdminSuccessResponse;
import moment.admin.dto.response.AdminUserDetailResponse;
import moment.admin.dto.response.AdminUserListResponse;
import moment.admin.service.user.AdminUserService;
import moment.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin User API", description = "관리자용 사용자 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserApiController {

    private final AdminUserService adminUserService;

    @Operation(summary = "사용자 목록 조회")
    @GetMapping
    public ResponseEntity<AdminSuccessResponse<Page<AdminUserListResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Page<User> users = adminUserService.getAllUsers(page, size);
        Page<AdminUserListResponse> response = users.map(AdminUserListResponse::from);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "사용자 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<AdminSuccessResponse<AdminUserDetailResponse>> getUser(
            @PathVariable Long id) {

        User user = adminUserService.getUserById(id);
        AdminUserDetailResponse response = AdminUserDetailResponse.from(user);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "사용자 정보 수정")
    @PutMapping("/{id}")
    public ResponseEntity<AdminSuccessResponse<AdminUserDetailResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {

        adminUserService.updateUser(id, request);
        User user = adminUserService.getUserById(id);
        AdminUserDetailResponse response = AdminUserDetailResponse.from(user);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "사용자 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<AdminSuccessResponse<Void>> deleteUser(
            @PathVariable Long id) {

        adminUserService.deleteUser(id);

        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }
}
