package moment.storage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파일 업로드 Url 응답")
public record UploadUrlResponse(
        @Schema(description = "S3 Presigned URL", example = "https://s3.amazonaws.com/bucket/images/abc123.jpg?X-Amz-...")
        String presignedUrl,

        @Schema(description = "저장된 파일 경로", example = "images/abc123.jpg")
        String filePath
) {
}
