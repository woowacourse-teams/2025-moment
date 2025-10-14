package moment.moment.dto.response.tobe;

import java.util.List;

public record MomentCompositions(
        List<MomentComposition> momentCompositionInfo,
        String nextCursor,
        boolean hasNextPage,
        int pageSize
) {
    public static MomentCompositions of(List<MomentComposition> momentCompositions,
                                        String nextCursor,
                                        boolean hasNextPage,
                                        int pageSize) {

        return new MomentCompositions(momentCompositions, nextCursor, hasNextPage, pageSize);
    }
}
