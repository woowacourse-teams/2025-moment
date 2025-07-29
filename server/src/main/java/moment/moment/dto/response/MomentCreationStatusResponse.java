package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.moment.domain.MomentCreationStatus;

@Schema(description = "모멘트 생성 가능 여부 상태")
public record MomentCreationStatusResponse(
        @Schema(description = "모멘트 생성 가능 여부 상태", example = "ALLOWED")
        MomentCreationStatus status
) {
    
}
