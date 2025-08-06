package moment.notification.presentation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.dto.response.SuccessResponse;
import moment.notification.application.NotificationService;
import moment.notification.dto.response.NotificationResponse;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@AuthenticationPrincipal Authentication authentication) {
        return notificationService.subscribe(authentication.id());
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal Authentication authentication,
            @RequestParam(value = "read", defaultValue = "false") Boolean read
    ) {
        List<NotificationResponse> responses = notificationService.getNotificationByUser(
                authentication.id(), read);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<SuccessResponse<Void>> patch(@PathVariable Long id) {
        notificationService.markAsRead(id);
        HttpStatus status = HttpStatus.NO_CONTENT;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
