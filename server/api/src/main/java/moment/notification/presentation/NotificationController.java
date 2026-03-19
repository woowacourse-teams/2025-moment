package moment.notification.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.notification.service.notification.SseNotificationService;
import moment.notification.dto.request.NotificationReadRequest;
import moment.notification.dto.response.NotificationResponse;
import moment.notification.service.application.NotificationApplicationService;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notification API", description = "알림 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notifications")
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;
    private final SseNotificationService sseNotificationService;

    @Operation(summary = "SSE 구독", description = "클라이언트가 SSE를 구독합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@AuthenticationPrincipal Authentication authentication, HttpServletResponse response) {
        response.setHeader("X-Accel-Buffering", "no");
        return sseNotificationService.subscribe(authentication.id());
    }

    @Operation(summary = "알림 조회", description = "사용자가 알림을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @GetMapping
    public ResponseEntity<SuccessResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal Authentication authentication,
            @RequestParam(value = "read", defaultValue = "false") Boolean read
    ) {
        List<NotificationResponse> responses = notificationApplicationService.getNotificationBy(
                authentication.id(), read);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }

    @Operation(summary = "알림 읽음", description = "사용자가 알림을 읽습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "알림 읽기 성공"),
            @ApiResponse(responseCode = "404", description = """
                    - [N-001] 존재하지 않는 알림입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<SuccessResponse<Void>> read(@PathVariable Long id) {
        notificationApplicationService.markAsRead(id);
        HttpStatus status = HttpStatus.NO_CONTENT;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "알림들 읽음", description = "사용자가 알림들을 읽습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "알림 읽기 성공"),
    })
    @PatchMapping("/read-all")
    public ResponseEntity<SuccessResponse<Void>> readAll(
            @Valid @RequestBody NotificationReadRequest notificationReadRequest) {
        notificationApplicationService.markAllAsRead(notificationReadRequest);
        HttpStatus status = HttpStatus.NO_CONTENT;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
