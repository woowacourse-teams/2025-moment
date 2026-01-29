package moment.moment.dto.response;

import java.util.List;

public record MyGroupMomentListResponse(
        List<MyGroupMomentResponse> moments,
        Long nextCursor,
        boolean hasNextPage
) {
    public static MyGroupMomentListResponse of(List<MyGroupMomentResponse> moments, Long nextCursor) {
        return new MyGroupMomentListResponse(moments, nextCursor, nextCursor != null);
    }

    public static MyGroupMomentListResponse empty() {
        return new MyGroupMomentListResponse(List.of(), null, false);
    }
}
