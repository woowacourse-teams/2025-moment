package moment.moment.dto.response;

import java.util.List;

public record GroupMomentListResponse(
    List<GroupMomentResponse> moments,
    Long nextCursor,
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
