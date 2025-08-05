package moment.moment.dto.response;

import java.util.List;

public record MyMomentPageResponse(
        List<MyMomentResponse> items,
        String nextCursor,
        boolean hasNextPage,
        int pageSize
) {
    public static MyMomentPageResponse of(
            List<MyMomentResponse> responses,
            String nextCursor,
            boolean hasNextPage,
            int pageSize
    ) {
        return new MyMomentPageResponse(responses, nextCursor, hasNextPage, pageSize);
    }
}
