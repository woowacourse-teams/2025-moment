package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.report.domain.Report;

public record MomentReportCreateResponse(
        @Schema(description = "신고 id", example = "1")
        Long id
) {

    public static MomentReportCreateResponse from(Report report) {
        return new MomentReportCreateResponse(report.getId());
    }
}
