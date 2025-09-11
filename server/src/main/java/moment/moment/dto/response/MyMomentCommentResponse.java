package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.reply.domain.Echo;
import moment.user.domain.Level;

@Schema(description = "모멘트에 달린 코멘트 응답")
public record MyMomentCommentResponse(
        @Schema(description = "코멘트 id", example = "1")
        Long id,

        @Schema(description = "코멘트 내용", example = "안됐네요.")
        String content,

        @Schema(description = "코멘터 닉네임", example = "미미")
        String nickname,

        @Schema(description = "코멘터 레벨", example = "ASTEROID_WHITE")
        Level level,

        @Schema(description = "코멘트 이미지 url", example = "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/moment-dev/images/2f501dfa-9c7d-4579-9c10-daed5a5da3ff%EA%B3%A0%EC%96%91%EC%9D%B4.jpg")
        String imageUrl,

        @Schema(description = "코멘트 작성 시간", example = "2025-07-14T16:30:34Z")
        LocalDateTime createdAt,

        List<MyMomentEchoResponse> echos
) {

    public static MyMomentCommentResponse of(Comment comment, List<Echo> echoes, CommentImage commentImage) {
        List<MyMomentEchoResponse> echoDetailResponse = echoes.stream()
                .map(MyMomentEchoResponse::from)
                .toList();

        String imageUrl = Optional.ofNullable(commentImage)
                .map(CommentImage::getImageUrl)
                .orElse(null);
        
        return new MyMomentCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCommenter().getNickname(),
                comment.getCommenter().getLevel(),
                imageUrl,
                comment.getCreatedAt(),
                echoDetailResponse);
    }
}
