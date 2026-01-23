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
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.request.GroupUpdateRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.group.dto.response.GroupDetailResponse;
import moment.group.dto.response.MyGroupResponse;
import moment.group.service.application.GroupApplicationService;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Group API", description = "그룹 관리 관련 API 명세")
@RestController
@RequestMapping("/api/v2/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupApplicationService groupApplicationService;

    @Operation(summary = "그룹 생성", description = "새로운 그룹을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "그룹 생성 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [U-009] 존재하지 않는 사용자입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<GroupCreateResponse> createGroup(
            @AuthenticationPrincipal Authentication authentication,
            @Valid @RequestBody GroupCreateRequest request) {
        GroupCreateResponse response = groupApplicationService.createGroup(authentication.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "내 그룹 목록 조회", description = "내가 속한 그룹 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 그룹 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    - [U-009] 존재하지 않는 사용자입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<MyGroupResponse>> getMyGroups(
            @AuthenticationPrincipal Authentication authentication) {
        List<MyGroupResponse> response = groupApplicationService.getMyGroups(authentication.id());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "그룹 상세 조회", description = "그룹의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 상세 조회 성공"),
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
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        GroupDetailResponse response = groupApplicationService.getGroupDetail(groupId, authentication.id());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "그룹 정보 수정", description = "그룹의 정보를 수정합니다. 그룹 소유자만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "그룹 정보 수정 성공"),
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
    @PatchMapping("/{groupId}")
    public ResponseEntity<Void> updateGroup(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId,
            @Valid @RequestBody GroupUpdateRequest request) {
        groupApplicationService.updateGroup(groupId, authentication.id(), request.name(), request.description());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "그룹 삭제", description = "그룹을 삭제합니다. 그룹 소유자만 가능하며, 멤버가 있으면 삭제할 수 없습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "그룹 삭제 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [GR-003] 멤버가 있는 그룹은 삭제할 수 없습니다.
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
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long groupId) {
        groupApplicationService.deleteGroup(groupId, authentication.id());
        return ResponseEntity.noContent().build();
    }
}
