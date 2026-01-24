package moment.moment.dto.response;

import java.util.List;

public record MyGroupFeedResponse(
        List<MyGroupMomentResponse> moments,
        Long nextCursor,
        boolean hasNextPage
) {
    public static MyGroupFeedResponse of(List<MyGroupMomentResponse> moments, Long nextCursor) {
        return new MyGroupFeedResponse(moments, nextCursor, nextCursor != null);
    }

    public static MyGroupFeedResponse empty() {
        return new MyGroupFeedResponse(List.of(), null, false);
    }
}
