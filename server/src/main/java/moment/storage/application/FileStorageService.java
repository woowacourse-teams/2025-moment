package moment.storage.application;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import moment.storage.dto.request.UploadUrlRequest;
import moment.storage.dto.response.UploadUrlResponse;
import moment.storage.infrastructure.AwsS3Client;
import moment.user.service.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final AwsS3Client awsS3Client;
    private final UserService userService;

    @Value("${s3.bucket-path}")
    private String bucketPath;

    public UploadUrlResponse getUploadUrl(UploadUrlRequest request, Long id) {
        userService.getUserBy(id);

        String extension = extractExtension(request.imageType());
        String filePath = bucketPath + "/" + UUID.randomUUID() + "." + extension;

        return awsS3Client.getUploadUrl(filePath);
    }

    private String extractExtension(String imageType) {
        if (imageType.contains("/")) {
            return imageType.substring(imageType.lastIndexOf("/") + 1);
        }
        return imageType;
    }
}
