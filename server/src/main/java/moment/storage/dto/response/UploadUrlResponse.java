package moment.storage.dto.response;

public record UploadUrlResponse(
        String presignedUrl,
        String filePath
) {
}
