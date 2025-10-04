package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.comment.dto.tobe.EchoDetail;

@Schema(description = "내 모멘트의 코멘트의 에코 조회 응답")
public record MyMomentEchoResponse(
        @Schema(description = "조회된 Echo id")
        Long id,

        @Schema(description = "조회된 Echo 타입")
        String echoType,

        @Schema(description = "조회된 Echo 작성자")
        String userName
) {

    public static MyMomentEchoResponse from(EchoDetail echoDetail) {
        return new MyMomentEchoResponse(echoDetail.id(), echoDetail.echoType(), echoDetail.userName());
    }
}
