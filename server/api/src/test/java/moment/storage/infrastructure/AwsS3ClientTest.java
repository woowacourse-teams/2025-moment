package moment.storage.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import moment.config.TestTags;
import moment.storage.dto.response.UploadUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@Tag(TestTags.UNIT)
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AwsS3ClientTest {

    @InjectMocks
    AwsS3Client awsS3Client;

    @Mock
    S3Presigner s3Presigner;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(awsS3Client, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(awsS3Client, "cloudfrontDomain", "https://cdn.example.com");
    }

    @Test
    void CloudFront_URL에_인코딩된_경로가_사용된다() throws MalformedURLException {
        // given
        String filePath = "test/images/a1b2c3d4-e5f6-7890-1234-567890abcdef.png";
        URL presignedUrl = new URL(
                "https://test-bucket.s3.amazonaws.com/test/images/a1b2c3d4-e5f6-7890-1234-567890abcdef.png"
                        + "?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=abc123"
        );
        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
        given(presignedRequest.url()).willReturn(presignedUrl);
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(presignedRequest);

        // when
        UploadUrlResponse response = awsS3Client.getUploadUrl(filePath);

        // then
        assertThat(response.filePath()).isEqualTo(
                "https://cdn.example.com/test/images/a1b2c3d4-e5f6-7890-1234-567890abcdef.png"
        );
    }

    @Test
    void 경로에_인코딩이_필요한_문자가_있으면_CloudFront_URL에서_인코딩이_유지된다() throws MalformedURLException {
        // given
        // URL 객체는 공백을 %20으로 인코딩된 상태로 생성
        String filePath = "test/images/file%20name.png";
        URL presignedUrl = new URL(
                "https://test-bucket.s3.amazonaws.com/test/images/file%20name.png"
                        + "?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=abc123"
        );
        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
        given(presignedRequest.url()).willReturn(presignedUrl);
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(presignedRequest);

        // when
        UploadUrlResponse response = awsS3Client.getUploadUrl(filePath);

        // then
        // getPath()는 %20을 공백으로 디코딩하므로 CloudFront URL에 공백이 들어감
        // getRawPath()는 %20을 유지하므로 CloudFront URL이 올바름
        assertThat(response.filePath()).doesNotContain(" ");
        assertThat(response.filePath()).isEqualTo(
                "https://cdn.example.com/test/images/file%20name.png"
        );
    }
}
