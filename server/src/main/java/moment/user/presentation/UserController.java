package moment.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.user.application.UserService;
import moment.user.dto.request.UserCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "사용자 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원 가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = """
                    - [U-004] 유효하지 않은 이메일 형식입니다.
                    - [U-005] 유효하지 않은 비밀번호 형식입니다.
                    - [U-006] 유효하지 않은 닉네임 형식입니다.
                    - [U-007] 비밀번호가 일치하지 않습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = """
                    - [U-001] 이미 가입된 사용자입니다.
                    - [U-003] 이미 존재하는 닉네임입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<Void>> createUser(@RequestBody UserCreateRequest request) {
        userService.addUser(request);
        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
