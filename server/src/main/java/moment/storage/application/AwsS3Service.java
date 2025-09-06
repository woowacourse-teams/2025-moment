package moment.storage.application;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import moment.storage.dto.response.UploadUrlResponse;
import moment.storage.infrastructure.AwsS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private final AwsS3Client awsS3Client;

    @Value("${s3.bucket-path}")
    private String bucketPath;

    public UploadUrlResponse getUploadUrl(String fileName) {
        String filePath = bucketPath + UUID.randomUUID() + fileName;

        return awsS3Client.getUploadUrl(filePath);
    }
}
