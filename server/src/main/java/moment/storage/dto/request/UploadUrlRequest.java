package moment.storage.dto.request;

import jakarta.validation.constraints.NotNull;

public record UploadUrlRequest(
        @NotNull
        String imageName,
        @NotNull
        String imageType
) {
}
