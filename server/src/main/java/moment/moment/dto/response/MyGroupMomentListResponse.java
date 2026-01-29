package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "그룹 내 나의 모멘트 목록 응답")
public record MyGroupMomentListResponse(
        @Schema(description = "나의 모멘트 목록")
        List<MyGroupMomentResponse> moments,

        @Schema(description = "다음 페이지 시작 커서", example = "123")
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNextPage
) {
    public static MyGroupMomentListResponse of(List<MyGroupMomentResponse> moments, Long nextCursor) {
        return new MyGroupMomentListResponse(moments, nextCursor, nextCursor != null);
    }

    public static MyGroupMomentListResponse empty() {
        return new MyGroupMomentListResponse(List.of(), null, false);
    }
}
