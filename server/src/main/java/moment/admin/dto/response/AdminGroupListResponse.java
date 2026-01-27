package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "그룹 목록 응답")
public record AdminGroupListResponse(
    @Schema(description = "그룹 목록")
    List<AdminGroupSummary> content,

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "20")
    int size,

    @Schema(description = "전체 요소 수", example = "150")
    long totalElements,

    @Schema(description = "전체 페이지 수", example = "8")
    int totalPages
) {
    public static AdminGroupListResponse from(Page<AdminGroupSummary> pageResult) {
        return new AdminGroupListResponse(
            pageResult.getContent(),
            pageResult.getNumber(),
            pageResult.getSize(),
            pageResult.getTotalElements(),
            pageResult.getTotalPages()
        );
    }
}
