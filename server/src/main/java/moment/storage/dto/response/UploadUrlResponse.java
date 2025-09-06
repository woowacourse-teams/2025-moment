package moment.storage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파일 업로드 Url 응답")
public record UploadUrlResponse(
        String presignedUrl,
        String filePath
) {
}
