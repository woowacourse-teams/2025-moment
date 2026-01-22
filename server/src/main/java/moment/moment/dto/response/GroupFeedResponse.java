package moment.moment.dto.response;

import java.util.List;

public record GroupFeedResponse(
    List<GroupMomentResponse> moments,
    Long nextCursor,
    boolean hasNextPage
) {
    public static GroupFeedResponse of(List<GroupMomentResponse> moments, Long nextCursor) {
        return new GroupFeedResponse(
            moments,
            nextCursor,
            nextCursor != null
        );
    }
}
