package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map.Entry;
import moment.comment.domain.Comment;
import moment.reply.domain.Emoji;

@Schema(description = "나의 Comment 목록 조회 응답")
public record MyCommentsResponse(
        @Schema(description = "등록된 Comment 내용", example = "정말 멋진 하루군요!")
        String content,

        @Schema(description = "Comment 등록 시간", example = "2025-07-21T10:57:08.926954")
        LocalDateTime createdAt,

        @Schema(description = "Comment가 등록된 Moment")
        MomentDetailResponse moment,

        @Schema(description = "Comment에 등록된 이모지 목록")
        List<EmojiDetailResponse> emojis
) {
    public static MyCommentsResponse from(Entry<Comment, List<Emoji>> entry) {
        Comment comment = entry.getKey();
        List<Emoji> emojis = entry.getValue();

        MomentDetailResponse momentResponse = MomentDetailResponse.from(comment.getMoment());

        List<EmojiDetailResponse> emojisResponse = emojis.stream()
                .map(EmojiDetailResponse::from)
                .toList();

        return new MyCommentsResponse(
                comment.getContent(),
                comment.getCreatedAt(),
                momentResponse,
                emojisResponse
        );
    }

    public static MyCommentsResponse from(Comment comment) {
        MomentDetailResponse momentResponse = MomentDetailResponse.from(comment.getMoment());
        List<EmojiDetailResponse> emojisResponse = null;
        return new MyCommentsResponse(
                comment.getContent(),
                comment.getCreatedAt(),
                momentResponse,
                emojisResponse
        );
    }
}
