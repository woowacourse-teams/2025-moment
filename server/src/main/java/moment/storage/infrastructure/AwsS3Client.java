package moment.storage.infrastructure;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import moment.storage.dto.response.UploadUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@RequiredArgsConstructor
public class AwsS3Client {

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(5);

    private final S3Presigner s3Presigner;

    @Value("${s3.bucket-name}")
    private String bucketName;

    public UploadUrlResponse getUploadUrl(String filePath) {
        PutObjectPresignRequest presignRequest = buildPresignedRequest(filePath);

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

        return new UploadUrlResponse(presignedUrl, presignedUrl.split("\\?")[0]);
    }

    private PutObjectPresignRequest buildPresignedRequest(String filePath) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();

        return PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_EXPIRATION)
                .putObjectRequest(putObjectRequest)
                .build();
    }
}
