package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.comment.domain.Comment;
import moment.reply.domain.Emoji;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map.Entry;

@Schema(description = "나의 Comment 목록 조회 응답")
public record MyCommentResponse(
        @Schema(description = "등록된 Comment id", example = "1")
        Long id,

        @Schema(description = "등록된 Comment 내용", example = "정말 멋진 하루군요!")
        String content,

        @Schema(description = "Comment 등록 시간", example = "2025-07-21T10:57:08.926954")
        LocalDateTime createdAt,

        @Schema(description = "Comment가 등록된 Moment")
        MomentDetailResponse moment,

        @Schema(description = "Comment에 등록된 이모지 목록")
        List<EmojiDetailResponse> emojis
) {
    public static MyCommentResponse from(Entry<Comment, List<Emoji>> commentAndEmojis) {
        Comment comment = commentAndEmojis.getKey();
        List<Emoji> emojis = commentAndEmojis.getValue();

        MomentDetailResponse momentResponse = MomentDetailResponse.from(comment.getMoment());

        List<EmojiDetailResponse> emojisResponse = emojis.stream()
                .map(EmojiDetailResponse::from)
                .toList();

        return new MyCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                momentResponse,
                emojisResponse
        );
    }

    public static MyCommentResponse from(Comment comment) {
        MomentDetailResponse momentResponse = MomentDetailResponse.from(comment.getMoment());
        List<EmojiDetailResponse> emojisResponse = null;
        return new MyCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                momentResponse,
                emojisResponse
        );
    }
}
