package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.moment.domain.Moment;

@Schema(description = "모멘트 정보")
public record AdminMomentResponse(
    @Schema(description = "모멘트 ID", example = "1")
    Long momentId,

    @Schema(description = "모멘트 내용", example = "오늘 하루도 열심히 보냈습니다!")
    String content,

    @Schema(description = "이미지 URL", example = "https://cdn.moment.com/images/moment1.jpg")
    String imageUrl,

    @Schema(description = "댓글 수", example = "10")
    int commentCount,

    @Schema(description = "좋아요 수", example = "25")
    int likeCount,

    @Schema(description = "작성자 정보")
    AdminMomentAuthorInfo author,

    @Schema(description = "생성 일시", example = "2024-01-20T14:30:00")
    LocalDateTime createdAt,

    @Schema(description = "삭제 일시 (삭제된 경우)", example = "2024-01-25T10:00:00")
    LocalDateTime deletedAt
) {
    public static AdminMomentResponse from(Moment moment, int commentCount) {
        return new AdminMomentResponse(
            moment.getId(),
            moment.getContent(),
            null,
            commentCount,
            0,
            AdminMomentAuthorInfo.from(moment.getMember()),
            moment.getCreatedAt(),
            moment.getDeletedAt()
        );
    }
}
