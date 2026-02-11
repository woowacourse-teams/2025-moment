package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "모멘트 목록 응답")
public record AdminMomentListResponse(
    @Schema(description = "모멘트 목록")
    List<AdminMomentResponse> content,

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "20")
    int size,

    @Schema(description = "전체 요소 수", example = "150")
    long totalElements,

    @Schema(description = "전체 페이지 수", example = "8")
    int totalPages
) {
}
