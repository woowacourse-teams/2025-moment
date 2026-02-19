package moment.storage.service.storage;

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

        String filePath = bucketPath + "/" + UUID.randomUUID() + "." + request.imageType();

        return awsS3Client.getUploadUrl(filePath);
    }
}
