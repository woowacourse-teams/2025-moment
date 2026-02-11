package moment.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 중복 확인 응답")
public record EmailConflictCheckResponse(
        @Schema(description = "중복 확인 결과", example = "false")
        boolean isExists
) {
}
