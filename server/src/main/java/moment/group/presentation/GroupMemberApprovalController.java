package moment.group.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.group.service.application.GroupMemberApplicationService;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Group Member Approval API", description = "그룹 멤버 승인/강퇴/소유권 이전 관련 API 명세")
@RestController
@RequestMapping("/api/v2/groups/{groupId}")
@RequiredArgsConstructor
public class GroupMemberApprovalController {

    private final GroupMemberApplicationService memberApplicationService;

    @Operation(summary = "멤버 강퇴", description = "그룹에서 멤버를 강퇴합니다. 그룹 소유자만 가능하며, 소유자는 강퇴할 수 없습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "멤버 강퇴 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [GM-006] 그룹 소유자는 강퇴할 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GR-002] 그룹 소유자가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    - [GM-001] 존재하지 않는 멤버입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<SuccessResponse<Void>> kickMember(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        memberApplicationService.kickMember(groupId, memberId, authentication.id());
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "멤버 승인", description = "대기 중인 멤버의 가입을 승인합니다. 그룹 소유자만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "멤버 승인 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [GM-004] 대기 중인 멤버가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GR-002] 그룹 소유자가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    - [GM-001] 존재하지 않는 멤버입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/members/{memberId}/approve")
    public ResponseEntity<SuccessResponse<Void>> approveMember(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        memberApplicationService.approveMember(groupId, memberId, authentication.id());
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "멤버 거절", description = "대기 중인 멤버의 가입을 거절합니다. 그룹 소유자만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "멤버 거절 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [GM-004] 대기 중인 멤버가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GR-002] 그룹 소유자가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    - [GM-001] 존재하지 않는 멤버입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/members/{memberId}/reject")
    public ResponseEntity<SuccessResponse<Void>> rejectMember(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        memberApplicationService.rejectMember(groupId, memberId, authentication.id());
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "소유권 이전", description = "그룹 소유권을 다른 멤버에게 이전합니다. 그룹 소유자만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "소유권 이전 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [GM-005] 승인된 멤버가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GR-002] 그룹 소유자가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    - [GM-001] 존재하지 않는 멤버입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/transfer/{memberId}")
    public ResponseEntity<SuccessResponse<Void>> transferOwnership(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        memberApplicationService.transferOwnership(groupId, authentication.id(), memberId);
        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
