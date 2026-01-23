package moment.group.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.dto.response.ErrorResponse;
import moment.group.dto.request.ProfileUpdateRequest;
import moment.group.dto.response.MemberResponse;
import moment.group.service.application.GroupMemberApplicationService;
import moment.user.dto.request.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Group Member API", description = "그룹 멤버 관리 관련 API 명세")
@RestController
@RequestMapping("/api/v2/groups/{groupId}")
@RequiredArgsConstructor
public class GroupMemberController {

    private final GroupMemberApplicationService memberApplicationService;

    @Operation(summary = "멤버 목록 조회", description = "그룹의 승인된 멤버 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "멤버 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GM-002] 그룹 멤버가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getMembers(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        List<MemberResponse> response = memberApplicationService.getMembers(groupId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "대기자 목록 조회", description = "그룹 가입 대기 중인 멤버 목록을 조회합니다. 그룹 소유자만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "대기자 목록 조회 성공"),
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
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/pending")
    public ResponseEntity<List<MemberResponse>> getPendingMembers(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        List<MemberResponse> response = memberApplicationService.getPendingMembers(groupId, authentication.id());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 프로필 수정", description = "그룹 내 나의 닉네임을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GM-002] 그룹 멤버가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = """
                    - [GM-008] 이미 사용 중인 닉네임입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        memberApplicationService.updateProfile(groupId, authentication.id(), request.nickname());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "그룹 탈퇴", description = "그룹에서 탈퇴합니다. 그룹 소유자는 탈퇴할 수 없습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "그룹 탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [GM-007] 그룹 소유자는 탈퇴할 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = """
                    - [GM-002] 그룹 멤버가 아닙니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [GR-001] 존재하지 않는 그룹입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/leave")
    public ResponseEntity<Void> leaveGroup(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        memberApplicationService.leaveGroup(groupId, authentication.id());
        return ResponseEntity.noContent().build();
    }

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
    public ResponseEntity<Void> kickMember(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        memberApplicationService.kickMember(groupId, memberId, authentication.id());
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<Void> approveMember(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        memberApplicationService.approveMember(groupId, memberId, authentication.id());
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<Void> rejectMember(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        memberApplicationService.rejectMember(groupId, memberId, authentication.id());
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<Void> transferOwnership(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        memberApplicationService.transferOwnership(groupId, authentication.id(), memberId);
        return ResponseEntity.noContent().build();
    }
}
