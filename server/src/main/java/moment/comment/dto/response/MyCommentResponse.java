package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.comment.domain.Comment;
import moment.moment.domain.MomentTag;
import moment.reply.domain.Echo;

import java.time.LocalDateTime;
import java.util.List;

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

        @Schema(description = "Comment에 등록된 에코 목록")
        List<EchoDetailResponse> echos
) {
    public static MyCommentResponse from(Comment comment, List<MomentTag> momentTags) {
        MomentDetailResponse momentResponse = MomentDetailResponse.from(comment.getMoment(), momentTags);
        List<EchoDetailResponse> echosResponse = null;
        return new MyCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                momentResponse,
                echosResponse
        );
    }

    public static MyCommentResponse from(Comment comment, List<Echo> echoes,  List<MomentTag> momentTags) {
        MomentDetailResponse momentResponse = MomentDetailResponse.from(comment.getMoment(), momentTags);
        List<EchoDetailResponse> echosResponse = echoes.stream()
                .map(EchoDetailResponse::from)
                .toList();

        return new MyCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                momentResponse,
                echosResponse
        );
    }
}
