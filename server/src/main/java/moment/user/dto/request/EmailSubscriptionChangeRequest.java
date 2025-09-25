package moment.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "이메일 구독 설정 변경 요청")
public record EmailSubscriptionChangeRequest(
        @Schema(description = "이메일 구독 설정", example = "false")
        @NotNull(message = "EMAIL_SUBSCRIPTION_INVALID")
        Boolean emailSubscription
) {
}
