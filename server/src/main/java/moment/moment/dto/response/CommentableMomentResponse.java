package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.user.domain.User;

@Schema(description = "코멘트를 달 수 있는 모멘트 응답")
public record CommentableMomentResponse(
        @Schema(description = "모멘트 id", example = "1")
        Long id,

        @Schema(description = "작성자 멤버 ID", example = "1")
        Long memberId,

        @Schema(description = "모멘트 작성자 이름", example = "미미")
        String nickname,

        @Schema(description = "모멘트 내용", example = "야근 힘들어용")
        String content,

        @Schema(description = "모멘트 이미지 경로" , example = "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/moment-dev/images/2f501dfa-9c7d-4579-9c10-daed5a5da3ff%EA%B3%A0%EC%96%91%EC%9D%B4.jpg")
        String imageUrl,

        @Schema(description = "모멘트 작성 시간", example = "2025-07-14T16:24:34Z")
        LocalDateTime createdAt
) {

    public static CommentableMomentResponse of(Moment moment, MomentImage momentImage) {
        String momentImageUrl = null;
        if (momentImage != null) {
            momentImageUrl = momentImage.getImageUrl();
        }

        User momenter = moment.getMomenter();
        String nickname = momenter != null ? momenter.getNickname() : "탈퇴한 사용자";

        return new CommentableMomentResponse(
                moment.getId(),
                moment.getMember() != null ? moment.getMember().getId() : null,
                nickname,
                moment.getContent(),
                momentImageUrl,
                moment.getCreatedAt());
    }

    public static CommentableMomentResponse empty() {
        return null;
    }
}
