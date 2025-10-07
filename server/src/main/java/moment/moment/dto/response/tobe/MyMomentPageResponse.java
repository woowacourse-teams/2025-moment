package moment.moment.dto.response.tobe;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import moment.comment.dto.tobe.CommentComposition;

@Schema(description = "나의 Moment 페이지 조회 응답")
public record MyMomentPageResponse(
        @Schema(description = "조회된 나의 Moment 목록 응답")
        MyMomentsResponse items,

        @Schema(description = "다음 페이지 시작 커서", example = "2025-07-21T10:57:08.926954_1")
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNextPage,

        @Schema(description = "페이지 사이즈 (기본 10)", example = "10")
        int pageSize
) {

    public static MyMomentPageResponse of(MomentCompositions momentCompositions,
                                          List<CommentComposition> commentCompositionInfo,
                                          Map<Long, List<Long>> unreadNotificationsByMomentIds) {

        return new MyMomentPageResponse(
                MyMomentsResponse.of(momentCompositions.momentCompositionInfo(), commentCompositionInfo, unreadNotificationsByMomentIds),
                momentCompositions.nextCursor(),
                momentCompositions.hasNextPage(),
                momentCompositions.pageSize());
    }
}
