package moment.notification.presentation;

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
import moment.global.dto.response.SuccessResponse;
import moment.notification.dto.request.DeviceEndpointRequest;
import moment.notification.service.application.PushNotificationApplicationService;
import moment.user.dto.request.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Push Notification API", description = "푸시 알림 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/push-notifications")
public class PushNotificationController {

    private final PushNotificationApplicationService pushNotificationApplicationService;

    @Operation(summary = "사용자 디바이스 정보 저장", description = "푸시 알림을 위한 사용자의 디바이스 정보를 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디바이스 정보 저장 성공"),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> registerDeviceEndpoint(
            @Valid @RequestBody DeviceEndpointRequest deviceEndpointRequest,
            @AuthenticationPrincipal Authentication authentication
    ) {
        pushNotificationApplicationService.registerDeviceEndpoint(authentication.id(), deviceEndpointRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "디바이스 정보 삭제", description = "푸시 알림을 위한 디바이스 정보를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디바이스 정보 삭제 성공"),
            @ApiResponse(responseCode = "404", description = """
                    - [U-009] 존재하지 않는 사용자입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping
    public ResponseEntity<SuccessResponse<Void>> deleteDeviceEndpoint(
            @Valid @RequestBody DeviceEndpointRequest deviceEndpointRequest,
            @AuthenticationPrincipal Authentication authentication
    ) {
        pushNotificationApplicationService.deleteDeviceEndpoint(authentication.id(), deviceEndpointRequest);
        return ResponseEntity.ok().build();
    }
}
