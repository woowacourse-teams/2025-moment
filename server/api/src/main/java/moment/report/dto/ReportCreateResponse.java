package moment.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReportCreateResponse(
        @Schema(description = "신고 id", example = "1")
        Long id
) {
}
