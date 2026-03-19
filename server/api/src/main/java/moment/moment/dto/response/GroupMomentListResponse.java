package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "그룹 모멘트 목록 응답")
public record GroupMomentListResponse(
    @Schema(description = "모멘트 목록")
    List<GroupMomentResponse> moments,

    @Schema(description = "다음 페이지 시작 커서", example = "123")
    Long nextCursor,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    boolean hasNextPage
) {
    public static GroupMomentListResponse of(List<GroupMomentResponse> moments, Long nextCursor) {
        return new GroupMomentListResponse(
            moments,
            nextCursor,
            nextCursor != null
        );
    }
}
