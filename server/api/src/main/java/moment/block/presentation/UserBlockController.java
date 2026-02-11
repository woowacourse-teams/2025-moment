package moment.block.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.block.dto.response.UserBlockListResponse;
import moment.block.dto.response.UserBlockResponse;
import moment.block.service.application.UserBlockApplicationService;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Block API", description = "사용자 차단 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/users")
public class UserBlockController {

    private final UserBlockApplicationService userBlockApplicationService;

    @Operation(summary = "사용자 차단", description = "특정 사용자를 차단합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "차단 성공"),
            @ApiResponse(responseCode = "400", description = "[BL-001] 자기 자신을 차단할 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "[U-009] 존재하지 않는 사용자입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "[BL-002] 이미 차단된 사용자입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{userId}/blocks")
    public ResponseEntity<SuccessResponse<UserBlockResponse>> blockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal Authentication authentication
    ) {
        UserBlockResponse response = userBlockApplicationService.blockUser(authentication.id(), userId);
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "사용자 차단 해제", description = "차단된 사용자를 차단 해제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "차단 해제 성공"),
            @ApiResponse(responseCode = "404", description = "[BL-003] 차단 관계가 존재하지 않습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{userId}/blocks")
    public ResponseEntity<Void> unblockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal Authentication authentication
    ) {
        userBlockApplicationService.unblockUser(authentication.id(), userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "차단 목록 조회", description = "내가 차단한 사용자 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/blocks")
    public ResponseEntity<SuccessResponse<List<UserBlockListResponse>>> getBlockedUsers(
            @AuthenticationPrincipal Authentication authentication
    ) {
        List<UserBlockListResponse> response = userBlockApplicationService.getBlockedUsers(authentication.id());
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
