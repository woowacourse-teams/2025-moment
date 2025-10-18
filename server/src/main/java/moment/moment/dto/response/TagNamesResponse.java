package moment.moment.dto.response;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "태그 이름 응답")
public record TagNamesResponse(
        @Schema(description = "태그 이름 리스트", example = "[\"일상/여가\", \"운동\"]")
        List<String> tagNames
) {
        @JsonValue
        public List<String> getTagNames() {
                return tagNames;
        }
}
