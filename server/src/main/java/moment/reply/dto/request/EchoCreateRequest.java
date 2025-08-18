package moment.reply.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Schema(description = "에코 등록 요청")
public record EchoCreateRequest(
        @Schema(description = "에코 타입 (중복 불허)", example = "[\"THANKS\", \"COMFORTED\"]")
        @NotEmpty(message = "에코 타입은 하나 이상 선택해야 합니다.")
        @Size(min = 1, max = 3, message = "에코 타입은 최소 {min}개, 최대 {max}개까지 선택할 수 있습니다.")
        Set<String> echoTypes,

        @Schema(description = "코멘트 id", example = "1")
        Long commentId
) {
}
