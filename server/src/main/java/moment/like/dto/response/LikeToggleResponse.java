package moment.like.dto.response;

public record LikeToggleResponse(
    boolean liked,
    long likeCount
) {
    public static LikeToggleResponse of(boolean liked, long likeCount) {
        return new LikeToggleResponse(liked, likeCount);
    }
}
