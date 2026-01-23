package moment.group.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.dto.response.ErrorResponse;
import moment.group.dto.request.GroupJoinRequest;
import moment.group.dto.response.GroupJoinResponse;
import moment.group.dto.response.InviteInfoResponse;
import moment.group.service.application.GroupMemberApplicationService;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Group Invite API", description = "그룹 초대 관련 API 명세")
@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class GroupInviteController {

    private final GroupMemberApplicationService memberApplicationService;

    @Operation(summary = "초대 링크 생성", description = "그룹 초대 링크를 생성합니다. 그룹 소유자만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "초대 링크 생성 성공"),
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
    @PostMapping("/groups/{groupId}/invite")
    public ResponseEntity<String> createInviteLink(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        String inviteCode = memberApplicationService.createInviteLink(groupId, authentication.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(inviteCode);
    }

    @Operation(summary = "초대 정보 조회", description = "초대 코드로 그룹 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초대 정보 조회 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [IL-002] 만료된 초대 링크입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [IL-001] 유효하지 않은 초대 링크입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/invite/{code}")
    public ResponseEntity<InviteInfoResponse> getInviteInfo(@PathVariable String code) {
        InviteInfoResponse response = memberApplicationService.getInviteInfo(code);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "그룹 가입 신청", description = "초대 코드를 사용하여 그룹에 가입을 신청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 가입 신청 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [IL-002] 만료된 초대 링크입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [IL-001] 유효하지 않은 초대 링크입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = """
                    - [GM-003] 이미 그룹 멤버입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/groups/join")
    public ResponseEntity<GroupJoinResponse> joinGroup(
            @AuthenticationPrincipal Authentication authentication,
            @Valid @RequestBody GroupJoinRequest request) {
        GroupJoinResponse response = memberApplicationService.joinGroup(authentication.id(), request);
        return ResponseEntity.ok(response);
    }
}
