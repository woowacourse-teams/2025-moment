package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "댓글 목록 응답")
public record AdminCommentListResponse(
    @Schema(description = "댓글 목록")
    List<AdminCommentResponse> content,

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "20")
    int size,

    @Schema(description = "전체 요소 수", example = "50")
    long totalElements,

    @Schema(description = "전체 페이지 수", example = "3")
    int totalPages
) {
}
