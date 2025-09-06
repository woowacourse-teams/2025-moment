package moment.storage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UploadUrlRequest(
        @Schema(description = "파일 이름", example = "my_cat.jpg")
        @NotBlank(message = "FILE_NAME_NOT_EXIST")
        String imageName,
        @Schema(description = "파일 타입", example = "jpg")
        @NotBlank(message = "FILE_TYPE_NOT_EXIST")
        String imageType
) {
}
