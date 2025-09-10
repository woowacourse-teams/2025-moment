package moment.storage.application;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import moment.storage.dto.request.UploadUrlRequest;
import moment.storage.dto.response.UploadUrlResponse;
import moment.storage.infrastructure.AwsS3Client;
import moment.user.application.UserQueryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final AwsS3Client awsS3Client;
    private final UserQueryService userQueryService;

    @Value("${s3.bucket-path}")
    private String bucketPath;

    public UploadUrlResponse getUploadUrl(UploadUrlRequest request, Long id) {
        userQueryService.getUserById(id);

        String filePath = bucketPath + UUID.randomUUID() + request.imageName();

        return awsS3Client.getUploadUrl(filePath);
    }
}
