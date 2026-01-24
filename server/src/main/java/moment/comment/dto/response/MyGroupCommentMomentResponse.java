package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.moment.dto.response.tobe.MomentComposition;

@Schema(description = "그룹 내 나의 Comment가 등록된 Moment 상세 내용")
public record MyGroupCommentMomentResponse(
        @Schema(description = "Moment 아이디", example = "1")
        Long id,

        @Schema(description = "Moment 내용", example = "테스트를 겨우 통과했어요!")
        String content,

        @Schema(description = "Moment 작성자 닉네임", example = "따뜻한 감성의 시리우스")
        String nickName,

        @Schema(description = "Moment 이미지 url", example = "https://example.com/image.jpg")
        String imageUrl,

        @Schema(description = "Moment 등록 시간", example = "2025-07-21T10:57:08.926954")
        LocalDateTime createdAt
) {
    public static MyGroupCommentMomentResponse from(MomentComposition momentComposition) {
        return new MyGroupCommentMomentResponse(
                momentComposition.id(),
                momentComposition.content(),
                momentComposition.nickname(),
                momentComposition.imageUrl(),
                momentComposition.momentCreatedAt()
        );
    }
}
