package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.moment.domain.Moment;

@Schema(description = "그룹 모멘트 응답")
public record GroupMomentResponse(
    @Schema(description = "모멘트 ID", example = "1")
    Long momentId,

    @Schema(description = "모멘트 내용", example = "오늘의 모멘트입니다")
    String content,

    @Schema(description = "작성자 닉네임", example = "닉네임")
    String memberNickname,

    @Schema(description = "작성자 멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "좋아요 수", example = "5")
    long likeCount,

    @Schema(description = "현재 사용자의 좋아요 여부", example = "false")
    boolean hasLiked,

    @Schema(description = "댓글 수", example = "3")
    long commentCount,

    @Schema(description = "이미지 URL", example = "https://example.com/images/sample.jpg")
    String imageUrl,

    @Schema(description = "생성 일시")
    LocalDateTime createdAt
) {
    public static GroupMomentResponse from(Moment moment, long likeCount, boolean hasLiked, long commentCount) {
        return new GroupMomentResponse(
            moment.getId(),
            moment.getContent(),
            moment.getMember() != null ? moment.getMember().getNickname() : null,
            moment.getMember() != null ? moment.getMember().getId() : null,
            likeCount,
            hasLiked,
            commentCount,
            null,
            moment.getCreatedAt()
        );
    }

    public static GroupMomentResponse from(Moment moment, long likeCount, boolean hasLiked, long commentCount, String imageUrl) {
        return new GroupMomentResponse(
            moment.getId(),
            moment.getContent(),
            moment.getMember() != null ? moment.getMember().getNickname() : null,
            moment.getMember() != null ? moment.getMember().getId() : null,
            likeCount,
            hasLiked,
            commentCount,
            imageUrl,
            moment.getCreatedAt()
        );
    }
}
