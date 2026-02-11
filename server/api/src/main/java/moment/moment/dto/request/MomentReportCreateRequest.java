package moment.moment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "모멘트 신고 요청")
public record MomentReportCreateRequest(
        @Schema(description = "신고사유", example = "개인정보 침해")
        @NotBlank(message = "REPORT_REASON_EMPTY")
        String reason
) {
}
