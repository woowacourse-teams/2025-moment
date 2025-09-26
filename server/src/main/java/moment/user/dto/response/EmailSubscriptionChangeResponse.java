package moment.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 구독 설정 변경 응답")
public record EmailSubscriptionChangeResponse(
        @Schema(description = "변경된 이메일 구독 설정 정보", example = "false")
        Boolean emailSubscription
) {
}

