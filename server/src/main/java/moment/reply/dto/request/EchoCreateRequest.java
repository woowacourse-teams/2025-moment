package moment.reply.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "에코 등록 요청")
public record EchoCreateRequest(
        @Schema(description = "에코 타입 (중복 불허)", example = "[\"THANKS\", \"COMFORTED\"]")
        Set<String> echoTypes,

        @Schema(description = "코멘트 id", example = "1")
        Long commentId){
}
