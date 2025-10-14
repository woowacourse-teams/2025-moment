package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.comment.domain.Echo;
import moment.comment.dto.tobe.EchoDetail;

@Schema(description = "Comment애 등록된 Echo 상세 내용")
public record EchoDetailResponse(
        @Schema(description = "Echo 아이디", example = "1")
        Long id,

        @Schema(description = "Echo 타입", example = "THANKS")
        String echoType,

        @Schema(description = "Echo를 등록한 유저 아이디", example = "2")
        Long userId
) {
    public static EchoDetailResponse from(EchoDetail echoDetail) {
        return new EchoDetailResponse(echoDetail.id(), echoDetail.echoType(), echoDetail.userId());
    }
}
