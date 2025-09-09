package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.report.domain.Report;

public record CommentReportCreateResponse(
        @Schema(description = "신고 id", example = "1")
        Long id
) {

    public static CommentReportCreateResponse from(Report report) {
        return new CommentReportCreateResponse(report.getId());
    }
}
