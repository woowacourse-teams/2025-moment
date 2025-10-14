package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Optional;
import moment.moment.domain.Moment;

import java.time.LocalDateTime;
import moment.moment.domain.MomentImage;
import moment.moment.domain.MomentTag;
import moment.moment.dto.response.tobe.MomentComposition;
import moment.user.domain.Level;

@Schema(description = "Comment가 등록된 Moment 상세 내용")
public record MomentDetailResponse(
        @Schema(description = "Moment 아이디", example = "1")
        Long id,

        @Schema(description = "Moment 내용", example = "테스트를 겨우 통과했어요!")
        String content,

        @Schema(description = "Moment 작성자 닉네임", example = "따뜻한 감성의 시리우스")
        String nickName,

        @Schema(description = "Moment 작성자 레벨", example = "METEOR")
        Level level,

        @Schema(description = "Moment 이미지 url", example = "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/moment-dev/images/2f501dfa-9c7d-4579-9c10-daed5a5da3ff%EA%B3%A0%EC%96%91%EC%9D%B4.jpg")
        String imageUrl,

        @Schema(description = "Moment 등록 시간", example = "2025-07-21T10:57:08.926954")
        LocalDateTime createdAt,

        @Schema(description = "Moment 태그", example = "일상/여가")
        List<String> tagNames
) {

    public static MomentDetailResponse from(MomentComposition momentComposition) {
        return new MomentDetailResponse(
                momentComposition.id(),
                momentComposition.content(),
                momentComposition.nickname(),
                momentComposition.level(),
                momentComposition.imageUrl(),
                momentComposition.momentCreatedAt(),
                momentComposition.tagNames()
        );
    }
}
